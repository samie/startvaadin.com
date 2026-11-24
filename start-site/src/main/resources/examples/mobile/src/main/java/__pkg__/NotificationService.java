package {{package}};

import java.math.BigInteger;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.webpush.WebPush;
import com.vaadin.flow.server.webpush.WebPushException;
import com.vaadin.flow.server.webpush.WebPushMessage;
import com.vaadin.flow.server.webpush.WebPushSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Notifications that reach the phone even when the app is closed, sent with Vaadin
 * Web Push. The subscriptions are kept in memory, so a restart forgets them; a real
 * app stores them in a database.
 */
@Service
public class NotificationService {

	private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

	private final Map<String, WebPushSubscription> subscriptions = new ConcurrentHashMap<>();

	private final WebPush webPush;

	private final String publicKey;

	NotificationService(@Value("${vapid.public-key:}") String publicKey,
			@Value("${vapid.private-key:}") String privateKey,
			@Value("${vapid.subject:mailto:admin@example.com}") String subject) {
		if (publicKey.isBlank()) {
			// No VAPID keys configured: a new pair for every run, fine for development
			KeyPair keys = generateKeyPair();
			ECPublicKey ecPublicKey = (ECPublicKey) keys.getPublic();
			// The uncompressed point: 0x04 followed by the x and y coordinates
			publicKey = base64(ByteBuffer.allocate(65)
				.put((byte) 0x04)
				.put(bytes32(ecPublicKey.getW().getAffineX()))
				.put(bytes32(ecPublicKey.getW().getAffineY()))
				.array());
			privateKey = base64(bytes32(((ECPrivateKey) keys.getPrivate()).getS()));
			logger.info("Generated VAPID keys for this run. After a restart, turn notifications off and on again, "
					+ "or set vapid.public-key and vapid.private-key to keep the keys.");
		}
		this.webPush = new WebPush(publicKey, privateKey, subject);
		this.publicKey = publicKey;
	}

	/**
	 * Tells why this browser cannot receive notifications, or {@code null} if it can.
	 */
	public void checkSupport(UI ui, Consumer<String> callback) {
		ui.getPage().executeJs("""
				if (!window.isSecureContext) {
				  return 'Notifications need a secure connection: HTTPS, or localhost.';
				}
				if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
				  return 'This browser has no push notifications. On an iPhone, add the app to the Home Screen first.';
				}
				return null;
				""").then(String.class, callback::accept);
	}

	public void isSubscribed(UI ui, Consumer<Boolean> callback) {
		this.webPush.subscriptionExists(ui, callback::accept);
	}

	/**
	 * Subscribes this browser, then tells why that failed, or {@code null} if it worked.
	 * The browser side runs here rather than in {@link WebPush#subscribe}, which reports
	 * every browser failure as a server error: a dismissed permission prompt, a service
	 * worker that is still installing or failed to, or an unreachable push service.
	 */
	public void subscribe(UI ui, Consumer<String> callback) {
		ui.getPage().executeJs("""
				try {
				  if (await Notification.requestPermission() !== 'granted') {
				    return 'Notifications are blocked for this site. Allow them in the browser settings.';
				  }
				  // The service worker that @PWA registers receives the notifications
				  const timeout = new Promise((resolve) => setTimeout(resolve, 20000));
				  const registration = await Promise.race([navigator.serviceWorker.ready, timeout]);
				  if (!registration) {
				    return 'The service worker has not started. Reload the page and try again.';
				  }
				  await registration.pushManager.subscribe({ userVisibleOnly: true, applicationServerKey: $0 });
				  return null;
				} catch (error) {
				  return 'Notifications could not be turned on: ' + error.message;
				}
				""", this.publicKey).then(String.class, (problem) -> {
			if (problem == null) {
				restore(ui);
			}
			callback.accept(problem);
		});
	}

	// Picks up a subscription the browser still has but this server has forgotten
	public void restore(UI ui) {
		this.webPush.fetchExistingSubscription(ui, this::store);
	}

	public void unsubscribe(UI ui) {
		this.webPush.unsubscribe(ui, (subscription) -> this.subscriptions.remove(subscription.endpoint()));
	}

	public void notifyAll(String title, String body) {
		List<WebPushSubscription> recipients = List.copyOf(this.subscriptions.values());
		if (recipients.isEmpty()) {
			logger.info("Not sending \"{}\": no browser has turned notifications on", title);
			return;
		}
		WebPushMessage message = new WebPushMessage(title, body);
		long sent = recipients.stream().filter((subscription) -> send(subscription, message)).count();
		logger.info("Sent \"{}\" to {} of {} subscribers", title, sent, recipients.size());
	}

	private void store(WebPushSubscription subscription) {
		this.subscriptions.put(subscription.endpoint(), subscription);
	}

	private boolean send(WebPushSubscription subscription, WebPushMessage message) {
		try {
			this.webPush.sendNotification(subscription, message);
			return true;
		}
		catch (WebPushException ex) {
			if (ex.getCause() != null) {
				// The push service could not be reached; the subscription may still work
				logger.warn("Could not send \"{}\" through {}: {}", message.title(), pushService(subscription),
						ex.getCause().toString());
			}
			else {
				// The push service answered 404 or 410: the browser unsubscribed or the subscription expired
				logger.info("Removing a subscription that {} no longer accepts", pushService(subscription));
				this.subscriptions.remove(subscription.endpoint());
			}
			return false;
		}
	}

	// The browser vendor's push service, such as web.push.apple.com, without the secret part of the address
	private static String pushService(WebPushSubscription subscription) {
		return URI.create(subscription.endpoint()).getHost();
	}

	private static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
			generator.initialize(new ECGenParameterSpec("secp256r1"));
			return generator.generateKeyPair();
		}
		catch (GeneralSecurityException ex) {
			throw new IllegalStateException(ex);
		}
	}

	// Exactly 32 bytes, as BigInteger may add a sign byte or drop leading zeros
	private static byte[] bytes32(BigInteger value) {
		byte[] raw = value.toByteArray();
		byte[] fixed = new byte[32];
		int length = Math.min(raw.length, 32);
		System.arraycopy(raw, raw.length - length, fixed, 32 - length, length);
		return fixed;
	}

	private static String base64(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}

}
