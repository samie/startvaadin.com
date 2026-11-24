package {{package}};

import java.util.Optional;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

	private final H1 title = new H1();

	private final Paragraph summary = new Paragraph();

	private final Markdown description = new Markdown();

	public ItemView(ItemService items) {
		this.items = items;

		RouterLink back = new RouterLink(HomeView.class);
		back.add(VaadinIcon.ANGLE_LEFT.create());
		back.getElement().setAttribute("aria-label", "Back to Home");
		back.addClassName("aura-surface");

		this.icon.addClassNames("aura-surface", "feature-icon");
		this.summary.addClassName("secondary");

		// One row: back button, icon, and the title with the summary under it, which wraps
		HorizontalLayout header = new HorizontalLayout(back, this.icon, new Div(this.title, this.summary));
		header.setAlignItems(Alignment.CENTER);
		header.setFlexShrink(0, back, this.icon);
		header.setWidthFull();
		header.addClassName("view-header");
		add(header, this.description);
	}

	@Override
	public void setParameter(BeforeEvent event, String id) {
		Optional<Item> item = this.items.findById(id);
		if (item.isEmpty()) {
			event.rerouteToError(NotFoundException.class);
			return;
		}
		this.icon.setIcon(item.get().icon());
		this.title.setText(item.get().title());
		this.summary.setText(item.get().summary());
		this.description.setContent(item.get().description());
	}

	@Override
	public String getPageTitle() {
		return this.title.getText();
	}

}
