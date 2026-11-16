package {{package}};

import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Section;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Home")
public class HomeView extends VerticalLayout {

	public HomeView(ItemService items) {
		Div cards = new Div();
		cards.addClassName("card-grid");
		items.findAll().forEach((item) -> cards.add(card(item)));

		add(new H1("Home"), new Section(new H2("Features"), cards));
	}

	// The whole card is the link, so the tap target is as big as the card
	private RouterLink card(Item item) {
		Card card = new Card();
		card.setTitle(item.title());
		card.setSubtitle(item.summary());

		RouterLink link = new RouterLink(ItemView.class, item.id());
		link.addClassName("card-link");
		link.add(card);
		return link;
	}

}
