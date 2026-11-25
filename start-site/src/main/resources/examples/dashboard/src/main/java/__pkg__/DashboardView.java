package {{package}};

import java.util.Locale;

import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("")
@PageTitle("Dashboard")
public class DashboardView extends VerticalLayout {

	public DashboardView() {
		HorizontalLayout header = new HorizontalLayout(JustifyContentMode.BETWEEN, new H1("Dashboard"),
				new ColorSchemeSelect());
		header.setAlignItems(Alignment.END);
		header.setWrap(true);
		header.setWidthFull();

		Div stats = new Div(stat("Users", "1,204"), stat("Sessions", "357"), stat("Revenue", "$12.8k"));
		stats.addClassName("stat-grid");
		stats.setWidthFull();

		add(header, stats, usage("CPU", 0.42), usage("Memory", 0.67), usage("Disk", 0.23));
	}

	private Card stat(String label, String value) {
		Card card = new Card();
		card.setTitle(value);
		card.setSubtitle(label);
		return card;
	}

	private VerticalLayout usage(String resource, double value) {
		NativeLabel label = new NativeLabel(resource);
		label.setId("usage-" + resource.toLowerCase(Locale.ROOT));
		HorizontalLayout caption = new HorizontalLayout(JustifyContentMode.BETWEEN, label,
				new Span(Math.round(value * 100) + "%"));
		caption.setWidthFull();

		ProgressBar bar = new ProgressBar();
		bar.setValue(value);
		// ProgressBar has no label of its own: name it after the label for screen readers
		bar.getElement().setAttribute("aria-labelledby", label.getId().orElseThrow());

		VerticalLayout row = new VerticalLayout(caption, bar);
		row.setPadding(false);
		row.setSpacing(false);
		return row;
	}

}
