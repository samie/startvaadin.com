package {{package}};

import com.vaadin.flow.component.icon.VaadinIcon;

public record Item(String id, VaadinIcon icon, String title, String summary, String description) {

}
