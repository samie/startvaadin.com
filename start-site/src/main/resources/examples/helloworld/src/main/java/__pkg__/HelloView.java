package {{package}};

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Hello")
public class HelloView extends VerticalLayout {

	public HelloView() {
		HorizontalLayout header = new HorizontalLayout(JustifyContentMode.BETWEEN, new H1("Hello World"),
				new ColorSchemeSelect());
		header.setAlignItems(Alignment.END);
		header.setWrap(true);
		header.setWidthFull();

		TextField name = new TextField("Your name");
		Button greet = new Button("Say hello", event -> Notification.show("Hello, " + name.getValue()));
		greet.addThemeVariants(ButtonVariant.PRIMARY);
		greet.addClickShortcut(Key.ENTER);
		add(header, name, greet);
	}

}
