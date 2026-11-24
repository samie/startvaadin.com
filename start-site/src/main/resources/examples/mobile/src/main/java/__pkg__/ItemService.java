package {{package}};

import java.util.List;
import java.util.Optional;

import com.vaadin.flow.component.icon.VaadinIcon;

import org.springframework.stereotype.Service;

/**
 * The data behind the list and detail views. An in-memory sample; a real app would read
 * from a repository here, and the views would not change.
 */
@Service
public class ItemService {

	// Descriptions are Markdown, which ItemView renders
	private final List<Item> items = List.of(
			new Item("tab-bar", VaadinIcon.MOBILE, "Bottom tab bar", "Navigation within thumb reach", """
					On phones, `MainLayout` shows its menu as a floating bar at the bottom of the screen.
					The selection slides with CSS only, in `styles.css`. Wider screens get a side menu instead.
					"""),
			new Item("drill-down", VaadinIcon.LEVEL_DOWN, "Drill-down view", "A detail page with a way back", """
					This page is `ItemView`, a route with a URL parameter. `HomeView` links each card to it.

					- The round button goes back to Home.
					- This text is Markdown, shown by the `Markdown` component.
					"""),
			new Item("instant-feedback", VaadinIcon.BOLT, "Instant feedback", "Taps respond before the server does", """
					`nav-pending.js` marks a tapped menu item as current at once and fades the old view,
					so moving around feels immediate even on a slow connection.
					"""),
			new Item("notifications", VaadinIcon.BELL, "Push notifications", "Messages that reach the phone", """
					Turn them on in **Profile**.

					- `NotificationService` subscribes the browser with Vaadin Web Push.
					- `DemoNotificationSender` sends one every few minutes.
					"""),
			new Item("appearance", VaadinIcon.ADJUST, "Light and dark", "Follows the system, or your choice", """
					Colours come from Aura theme properties, so both schemes work without extra CSS.
					**Profile** has a setting for trying them out.
					"""),
			new Item("installable", VaadinIcon.PLUS_SQUARE_O, "Installable app", "Add it to the home screen", """
					`@PWA` on `AppShell` makes the app installable and registers the service worker,
					which also receives the notifications. On an iPhone, use *Share* and then *Add to Home Screen*.
					"""));

	public List<Item> findAll() {
		return this.items;
	}

	public Optional<Item> findById(String id) {
		return this.items.stream().filter((item) -> item.id().equals(id)).findFirst();
	}

}
