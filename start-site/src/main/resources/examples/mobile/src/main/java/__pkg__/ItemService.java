package {{package}};

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

/**
 * The data behind the list and detail views. An in-memory sample; a real app would read
 * from a repository here, and the views would not change.
 */
@Service
public class ItemService {

	private final List<Item> items = List.of(
			new Item("tab-bar", "Bottom tab bar", "Navigation within thumb reach",
					"On phones, MainLayout shows its menu as a floating bar at the bottom of the screen, and the "
							+ "selection slides with CSS only (styles.css). Wider screens get a side menu instead."),
			new Item("drill-down", "Drill-down view", "A detail page with a way back",
					"This page is ItemView: a route with a URL parameter, a back button that stays at the top while "
							+ "you scroll, and a centred title. HomeView links each card to it."),
			new Item("instant-feedback", "Instant feedback", "Taps respond before the server does",
					"nav-pending.js marks a tapped menu item as current at once and fades the old view, so moving "
							+ "around feels immediate even on a slow connection."),
			new Item("notifications", "Push notifications", "Messages that reach the phone",
					"Turn them on in Profile. NotificationService subscribes the browser with Vaadin Web Push, and "
							+ "DemoNotificationSender sends one every few minutes."),
			new Item("appearance", "Light and dark", "Follows the system, or your choice",
					"Colours come from Aura theme properties, so both schemes work without extra CSS. Profile has a "
							+ "switch for trying them out."),
			new Item("installable", "Installable app", "Add it to the home screen",
					"@PWA on AppShell makes the app installable and registers the service worker, which is also "
							+ "what receives the notifications."));

	public List<Item> findAll() {
		return this.items;
	}

	public Optional<Item> findById(String id) {
		return this.items.stream().filter((item) -> item.id().equals(id)).findFirst();
	}

}
