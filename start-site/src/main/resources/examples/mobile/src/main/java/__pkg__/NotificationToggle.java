package {{package}};

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Switch;
import com.vaadin.flow.component.notification.Notification;

public class NotificationToggle extends Switch {

	private final NotificationService notifications;

	public NotificationToggle(NotificationService notifications) {
		super("Notifications");
		this.notifications = notifications;
		// Until the browser has told whether it is subscribed
		setEnabled(false);
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
				setHelperText(problem);
				return;
			}
			this.notifications.isSubscribed(ui, (subscribed) -> {
				setValue(subscribed);
				setEnabled(true);
				if (subscribed) {
					this.notifications.restore(ui);
				}
			});
		});
	}

	private void subscribe(UI ui) {
		this.notifications.subscribe(ui, (problem) -> {
			if (problem != null) {
				setValue(false);
			}
			Notification.show((problem != null) ? problem : "Notifications are on. The first one arrives within minutes.");
		});
	}

}
