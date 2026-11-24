package {{package}};

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

/**
 * Sends a demo notification to every subscriber every one to three minutes. Remove
 * it once the app sends notifications of its own.
 */
@Service
public class DemoNotificationSender {

	private static final Logger logger = LoggerFactory.getLogger(DemoNotificationSender.class);

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private final NotificationService notifications;

	DemoNotificationSender(NotificationService notifications) {
		this.notifications = notifications;
		scheduleNext();
	}

	private void scheduleNext() {
		int minutes = ThreadLocalRandom.current().nextInt(1, 4);
		this.scheduler.schedule(this::send, minutes, TimeUnit.MINUTES);
	}

	private void send() {
		try {
			String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
			this.notifications.notifyAll("Demo notification", "Sent at " + time + ". The next one comes in 1–3 minutes.");
		}
		catch (RuntimeException ex) {
			// The scheduler would swallow it silently
			logger.error("Sending the demo notification failed", ex);
		}
		finally {
			scheduleNext();
		}
	}

	@PreDestroy
	void stop() {
		this.scheduler.shutdownNow();
	}

}
