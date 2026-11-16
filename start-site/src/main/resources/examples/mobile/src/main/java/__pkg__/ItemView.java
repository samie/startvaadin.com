package {{package}};

import java.util.Optional;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "item", layout = MainLayout.class)
public class ItemView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {

	private final ItemService items;

	private final H1 title = new H1();

	private final Paragraph summary = new Paragraph();

	private final Paragraph description = new Paragraph();

	public ItemView(ItemService items) {
		this.items = items;

		RouterLink back = new RouterLink(HomeView.class);
		back.add(VaadinIcon.ANGLE_LEFT.create());
		back.getElement().setAttribute("aria-label", "Back to Home");
		back.addClassName("aura-surface");

		Header header = new Header(back, title);
		header.addClassName("view-header");

		summary.addClassName("secondary");
		add(header, summary, description);
	}

	@Override
	public void setParameter(BeforeEvent event, String id) {
		Optional<Item> item = this.items.findById(id);
		if (item.isEmpty()) {
			event.rerouteToError(NotFoundException.class);
			return;
		}
		title.setText(item.get().title());
		summary.setText(item.get().summary());
		description.setText(item.get().description());
	}

	@Override
	public String getPageTitle() {
		return title.getText();
	}

}
