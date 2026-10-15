package {{package}};

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.Route;

@Route("")
public class DashboardView extends VerticalLayout {

	public DashboardView() {
		add(new H2("Dashboard"));
		add(new HorizontalLayout(stat("Users", "1,204"), stat("Sessions", "357"), stat("Revenue", "$12.8k")));
		add(progress("CPU", 0.42), progress("Memory", 0.67), progress("Disk", 0.23));
	}

	private VerticalLayout stat(String label, String value) {
		VerticalLayout card = new VerticalLayout(new H3(value), new Span(label));
		card.setSpacing(false);
		return card;
	}

	private VerticalLayout progress(String label, double value) {
		ProgressBar bar = new ProgressBar();
		bar.setValue(value);
		VerticalLayout row = new VerticalLayout(new Span(label), bar);
		row.setSpacing(false);
		return row;
	}

}
