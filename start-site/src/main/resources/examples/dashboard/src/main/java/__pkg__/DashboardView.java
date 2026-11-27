package {{package}};

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.checkbox.Switch;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.local.ValueSignal;

import {{package}}.SystemMetrics.Sample;
import {{package}}.TrendChartWidget.Series;

@Route("")
@PageTitle("JVM Dashboard")
public class DashboardView extends VerticalLayout {

	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

	private static final double MB = 1024 * 1024;

	private final ValueSignal<Boolean> paused = new ValueSignal<>(false);

	public DashboardView(SystemMetrics metrics) {
		// The samples the widgets show: the shared history, held at its last value while paused
		Signal<List<Sample>> history = metrics.history();
		Signal<List<Sample>> samples = Signal.computed(() -> this.paused.get() ? history.peek() : history.get());

		// Two responsive grids in styles.css: four key figures, then the wide widgets in pairs
		Div keyFigures = new Div(
				new MetricWidget("CPU load", samples, Sample::systemCpu, (ratio) -> Math.round(ratio * 100) + "%"),
				new MetricWidget("Heap used", samples, Sample::heapUsed, (bytes) -> Math.round(bytes / MB) + " MB"),
				new MetricWidget("Live threads", samples, Sample::threads, (count) -> String.valueOf(Math.round(count))),
				new UptimeWidget(samples));
		keyFigures.addClassName("metric-grid");
		keyFigures.setWidthFull();
		TrendChartWidget cpuTrend = new TrendChartWidget("CPU trend", "%", samples,
				new Series("System", (sample) -> sample.systemCpu() * 100),
				new Series("Process", (sample) -> sample.processCpu() * 100));
		TrendChartWidget heapTrend = new TrendChartWidget("Heap trend", " MB", samples,
				new Series("Used", (sample) -> sample.heapUsed() / MB),
				new Series("Committed", (sample) -> sample.heapCommitted() / MB));
		Div wideWidgets = new Div(cpuTrend, heapTrend, new ResourceUsageWidget(samples),
				new SystemInfoWidget(samples));
		wideWidgets.addClassName("wide-grid");
		wideWidgets.setWidthFull();

		addClassName("dashboard");
		add(header(samples), keyFigures, wideWidgets);
	}

	private HorizontalLayout header(Signal<List<Sample>> samples) {
		Badge live = new Badge(this.paused.map((paused) -> paused ? "Paused" : "Live"), VaadinIcon.CIRCLE.create());
		live.bindThemeVariant(BadgeVariant.SUCCESS, Signal.not(this.paused));
		live.addClassName("live");
		Span updated = new Span();
		updated.bindText(samples
			.map((shown) -> "Updated " + TIME.format(shown.getLast().time().atZone(ZoneId.systemDefault()))));
		HorizontalLayout status = new HorizontalLayout(live, updated);
		status.setAlignItems(Alignment.CENTER);
		// The OpenJDK logo (simple-icons, CC0) takes the text colour; decorative, as the H1 names the page
		SvgIcon logo = new SvgIcon("images/openjdk.svg");
		logo.addClassName("logo");
		VerticalLayout title = new VerticalLayout(new H1(logo, new Text("JVM Dashboard")), status);
		title.setPadding(false);
		title.setSpacing(false);
		title.addClassName("dashboard-title");
		title.setWidth(null);

		Switch pause = new Switch("Pause");
		pause.bindValue(this.paused, this.paused::set);
		// Grouped, so that on a phone they wrap to the next row together
		HorizontalLayout controls = new HorizontalLayout(pause, new ColorSchemeSelect());
		controls.setAlignItems(Alignment.END);
		HorizontalLayout header = new HorizontalLayout(title);
		header.addToEnd(controls);
		header.setAlignItems(Alignment.END);
		header.setWrap(true);
		header.setWidthFull();
		return header;
	}

}
