package {{package}};

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("")
public class HelloView extends VerticalLayout {

	public HelloView() {
		TextField name = new TextField("Your name");
		Button greet = new Button("Say hello", event -> Notification.show("Hello, " + name.getValue()));
		add(name, greet);
	}

}
