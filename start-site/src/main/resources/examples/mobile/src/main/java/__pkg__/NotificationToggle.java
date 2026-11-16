package {{package}};

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.notification.Notification;

public class NotificationToggle extends Checkbox {

	private final NotificationService notifications;

	public NotificationToggle(NotificationService notifications) {
		super("Send me notifications");
		this.notifications = notifications;
		addValueChangeListener((event) -> {
			if (!event.isFromClient()) {
				return;
			}
			UI ui = event.getSource().getUI().orElseThrow();
			if (event.getValue()) {
				subscribe(ui);
			}
			else {
				notifications.unsubscribe(ui);
			}
		});
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		UI ui = attachEvent.getUI();
		this.notifications.checkSupport(ui, (problem) -> {
			if (problem != null) {
				setEnabled(false);
				setHelperText(problem);
				return;
			}
			this.notifications.isSubscribed(ui, (subscribed) -> {
				setValue(subscribed);
				if (subscribed) {
					this.notifications.restore(ui);
				}
			});
		});
	}

	private void subscribe(UI ui) {
		this.notifications.isBlocked(ui, (blocked) -> {
			if (blocked) {
				setValue(false);
				Notification.show("Notifications are blocked for this site. Allow them in the browser settings.");
			}
			else {
				this.notifications.subscribe(ui);
			}
		});
	}

}
