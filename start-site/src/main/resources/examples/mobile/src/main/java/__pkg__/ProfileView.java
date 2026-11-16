package {{package}};

import java.util.Map;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profile")
public class ProfileView extends VerticalLayout {

	private static final Map<ColorScheme.Value, String> APPEARANCES = Map.of(ColorScheme.Value.LIGHT_DARK, "System",
			ColorScheme.Value.LIGHT, "Light", ColorScheme.Value.DARK, "Dark");

	public ProfileView(NotificationService notifications) {
		TextField name = new TextField("Name");
		EmailField email = new EmailField("Email");

		Button save = new Button("Save", (event) -> Notification.show("Profile saved"));
		save.addThemeVariants(ButtonVariant.PRIMARY);

		add(new H1("Profile"),
				new FormLayout(name, email, new NotificationToggle(notifications), appearance()), save);
	}

	// Switches this browser tab between light and dark; a reload returns to the system setting
	private RadioButtonGroup<ColorScheme.Value> appearance() {
		RadioButtonGroup<ColorScheme.Value> appearance = new RadioButtonGroup<>("Appearance");
		appearance.setItems(ColorScheme.Value.LIGHT_DARK, ColorScheme.Value.LIGHT, ColorScheme.Value.DARK);
		appearance.setItemLabelGenerator(APPEARANCES::get);
		ColorScheme.Value current = UI.getCurrentOrThrow().getPage().getColorScheme();
		appearance.setValue(APPEARANCES.containsKey(current) ? current : ColorScheme.Value.LIGHT_DARK);
		appearance.addValueChangeListener((event) -> UI.getCurrentOrThrow().getPage().setColorScheme(event.getValue()));
		return appearance;
	}

}
