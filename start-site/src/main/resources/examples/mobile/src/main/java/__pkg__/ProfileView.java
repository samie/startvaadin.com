package {{package}};

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profile")
public class ProfileView extends VerticalLayout {

	public ProfileView(NotificationService notifications) {
		TextField name = new TextField("Name");
		EmailField email = new EmailField("Email");

		Button save = new Button("Save", (event) -> Notification.show("Profile saved"));
		save.addThemeVariants(ButtonVariant.PRIMARY);

		add(new H1("Profile"),
				new FormLayout(name, email, new NotificationToggle(notifications), new ColorSchemeSelect()), save);
	}

}
