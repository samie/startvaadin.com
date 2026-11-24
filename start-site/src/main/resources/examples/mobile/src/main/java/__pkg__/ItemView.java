package {{package}};

import java.util.Optional;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
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

	private final Icon icon = new Icon();

	private final Text name = new Text("");

	private final H1 title = new H1(this.icon, this.name);

	private final Paragraph summary = new Paragraph();

	private final Paragraph description = new Paragraph();

	public ItemView(ItemService items) {
		this.items = items;

		RouterLink back = new RouterLink(HomeView.class);
		back.add(VaadinIcon.ANGLE_LEFT.create());
		back.getElement().setAttribute("aria-label", "Back to Home");
		back.addClassName("aura-surface");

		Header header = new Header(back, this.title);
		header.addClassName("view-header");

		this.icon.addClassNames("aura-surface", "feature-icon");
		this.summary.addClassName("secondary");
		add(header, this.summary, this.description);
	}

	@Override
	public void setParameter(BeforeEvent event, String id) {
		Optional<Item> item = this.items.findById(id);
		if (item.isEmpty()) {
			event.rerouteToError(NotFoundException.class);
			return;
		}
		this.icon.setIcon(item.get().icon());
		this.name.setText(item.get().title());
		this.summary.setText(item.get().summary());
		this.description.setText(item.get().description());
	}

	@Override
	public String getPageTitle() {
		return this.name.getText();
	}

}
