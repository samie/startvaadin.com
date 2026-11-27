/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.shared.Registration;
import io.spring.start.site.StartConfigurationProperties;
import io.spring.start.site.stats.Count;
import io.spring.start.site.stats.JvmMetrics;
import io.spring.start.site.stats.StatisticsService;
import org.vaadin.svgvis.SvgSparkLine;
import org.vaadin.svgvis.SvgSparkLine.DataPoint;

/**
 * A light-hearted live usage dashboard for the curious. Served at {@code /statistics}
 * (plus a {@code /stats} alias) by Vaadin's root mapping. Mirrors {@link MainView}'s
 * shell (Aura theme + dark-mode toggle). Key figures, a 24-hour activity chart and live
 * server figures ({@link JvmMetrics}) are built once and updated in place on every poll,
 * so the page ticks without flicker; only the breakdown bars are rebuilt.
 *
 * @author Sami Ekblad
 */
@Route("statistics")
@RouteAlias("stats")
@PageTitle("Statistics · " + Branding.NAME)
public final class StatisticsView extends AppLayout {

	private static final int TOP_DEPENDENCIES = 12;

	private static final int ACTIVITY_HOURS = 24;

	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)
		.withZone(ZoneId.systemDefault());

	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)
		.withZone(ZoneId.systemDefault());

	private final StatisticsService statistics;

	private final JvmMetrics jvmMetrics;

	private final InitializrUiService service;

	private final Preferences preferences;

	private final int pollMillis;

	private final Button themeToggle;

	private final VerticalLayout content;

	private final Span updated = new Span();

	private final Card generated = kpi("Projects generated");

	private final SvgSparkLine activity = sparkLine(null);

	private final Card average = kpi("Average / day");

	private final Card last = kpi("Last generated");

	private final Card since = kpi("Counting since");

	private final Card cpu = kpi("CPU load");

	private final SvgSparkLine cpuChart = sparkLine(" %");

	private final Card heap = kpi("Heap used");

	private final SvgSparkLine heapChart = sparkLine(" MB");

	private final Card threads = kpi("Live threads");

	private final Card uptime = kpi("Uptime");

	private final Div breakdowns = new Div();

	private boolean darkMode = true;

	private Registration pollRegistration;

	public StatisticsView(StatisticsService statistics, JvmMetrics jvmMetrics, InitializrUiService service,
			Preferences preferences, StartConfigurationProperties properties) {
		this.statistics = statistics;
		this.jvmMetrics = jvmMetrics;
		this.service = service;
		this.preferences = preferences;
		this.pollMillis = Math.max(2, properties.getStatistics().getPollIntervalSeconds()) * 1000;
		this.themeToggle = new Button(new Icon(VaadinIcon.SUN), (event) -> toggleTheme());
		this.themeToggle.addThemeVariants(ButtonVariant.TERTIARY);
		this.themeToggle.setAriaLabel("Switch to light mode");
		this.themeToggle.setTooltipText("Switch to light mode");
		buildNavbar();
		this.content = new VerticalLayout();
		this.content.addClassName("stats-content");
		this.content.add(liveStrip());
		this.generated.add(this.activity);
		this.content.add(section("Projects", kpis(this.generated, this.average, this.last, this.since)));
		this.cpu.add(this.cpuChart);
		this.heap.add(this.heapChart);
		this.content.add(section("Server", kpis(this.cpu, this.heap, this.threads, this.uptime)));
		this.breakdowns.addClassName("stats-breakdowns");
		this.content.add(this.breakdowns);
		setContent(this.content);
		refresh();
	}

	private Component liveStrip() {
		Badge live = new Badge("Live");
		live.addThemeVariants(BadgeVariant.SUCCESS);
		live.addClassName("live-badge");
		this.updated.addClassName("stats-updated");
		HorizontalLayout strip = new HorizontalLayout(live, this.updated);
		strip.setAlignItems(FlexComponent.Alignment.CENTER);
		strip.addClassName("stats-live");
		return strip;
	}

	private static Card kpi(String label) {
		Card card = new Card();
		card.addClassName("stats-kpi");
		card.setSubtitle(label);
		return card;
	}

	private static SvgSparkLine sparkLine(String unit) {
		SvgSparkLine chart = new SvgSparkLine(72);
		chart.addClassName("stats-chart");
		chart.setUnit(unit);
		// Straight segments: a smoothed curve would overshoot the real minimum and
		// maximum
		chart.setUseBezierCurve(false);
		return chart;
	}

	private static Component kpis(Card... cards) {
		Div grid = new Div(cards);
		grid.addClassName("stats-kpis");
		return grid;
	}

	private void buildNavbar() {
		RouterLink back = new RouterLink("← Generator", MainView.class);
		back.getStyle().set("font-size", "0.875rem").set("margin-left", "var(--vaadin-gap-m, 0.75rem)");
		H1 title = new H1("Statistics");
		title.getStyle().set("font-size", "1.125rem").set("margin", "0 0 0 var(--vaadin-gap-m, 0.75rem)");
		HorizontalLayout right = new HorizontalLayout(this.themeToggle);
		right.setAlignItems(FlexComponent.Alignment.CENTER);
		right.getStyle().set("margin-left", "auto").set("margin-right", "var(--vaadin-padding-l, 1rem)");
		addToNavbar(back, title, right);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		this.preferences.loadTheme(this::applyTheme);
		UI ui = attachEvent.getUI();
		ui.setPollInterval(this.pollMillis);
		this.pollRegistration = ui.addPollListener((event) -> refresh());
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		if (this.pollRegistration != null) {
			this.pollRegistration.remove();
			this.pollRegistration = null;
		}
		detachEvent.getUI().setPollInterval(-1);
		super.onDetach(detachEvent);
	}

	private void refresh() {
		this.updated.setText("Updated " + TIME.format(Instant.now()));
		refreshProjects();
		refreshServer();
		refreshBreakdowns();
	}

	private void refreshProjects() {
		long total = this.statistics.total();
		long[] perHour = this.statistics.generationsPerHour(ACTIVITY_HOURS);
		this.generated.setTitle(formatCount(total));
		this.activity.setData(Arrays.stream(perHour).asDoubleStream().toArray());
		this.activity.setTitle(formatCount(Arrays.stream(perHour).sum()) + " in the last 24 h");
		this.average.setTitle(formatCount(Math.round(this.statistics.perDayAverage())));
		this.last.setTitle(relative(this.statistics.lastGenerated()));
		this.since.setTitle(DATE.format(this.statistics.countingSince()));
	}

	private void refreshServer() {
		List<JvmMetrics.Sample> history = this.jvmMetrics.history();
		JvmMetrics.Sample latest = history.get(history.size() - 1);
		this.cpu.setTitle(Math.round(latest.cpuLoad() * 100) + " %");
		this.cpuChart
			.setData(history.stream().map((sample) -> DataPoint.of(sample.time(), sample.cpuLoad() * 100)).toList());
		this.heap.setTitle(megabytes(latest.heapUsed()));
		this.heap.setSubtitle(
				"Heap used · " + Math.round(latest.heapShare() * 100) + " % of " + megabytes(latest.heapMax()));
		this.heapChart.setData(history.stream()
			.map((sample) -> DataPoint.of(sample.time(), sample.heapUsed() / 1_048_576.0))
			.toList());
		this.threads.setTitle(formatCount(latest.threads()));
		this.uptime.setTitle(duration(this.jvmMetrics.uptime()));
	}

	private void refreshBreakdowns() {
		this.breakdowns.removeAll();
		if (this.statistics.total() == 0) {
			this.breakdowns.add(emptyState());
			return;
		}
		this.breakdowns.add(section("Top dependencies", bars(this.statistics.topDependencies(TOP_DEPENDENCIES), true)));
		this.breakdowns.add(section("Spring Boot versions", bars(this.statistics.bootVersionDistribution(), false)));
		this.breakdowns.add(section("Java versions", bars(this.statistics.javaVersionDistribution(), false)));
		this.breakdowns.add(section("Build systems", bars(this.statistics.buildSystemDistribution(), false)));
		this.breakdowns.add(section("Languages", bars(this.statistics.languageDistribution(), false)));
		this.breakdowns.add(section("Packaging", bars(this.statistics.packagingDistribution(), false)));
		this.breakdowns.add(section("Generate vs Explore", bars(this.statistics.uiActionDistribution(), false)));
		this.breakdowns.add(section("Arrived from", bars(this.statistics.referrerDistribution(), false)));
	}

	private static Component section(String title, Component body) {
		VerticalLayout section = new VerticalLayout();
		section.addClassName("stat-section");
		section.setPadding(false);
		section.setSpacing(false);
		H2 heading = new H2(title);
		section.add(heading, body);
		return section;
	}

	private Component bars(List<Count> counts, boolean resolveDependencyNames) {
		if (counts.isEmpty()) {
			Span none = new Span("No data yet.");
			none.addClassName("stats-none");
			return none;
		}
		long max = counts.stream().mapToLong(Count::count).max().orElse(1);
		Div bars = new Div();
		bars.addClassName("bars");
		for (Count count : counts) {
			bars.add(bar(count, max, resolveDependencyNames));
		}
		return bars;
	}

	private Component bar(Count count, long max, boolean resolveDependencyName) {
		String label = resolveDependencyName
				? this.service.getDependency(count.id()).map((dependency) -> dependency.getName()).orElse(count.id())
				: count.id();
		Div row = new Div();
		row.addClassName("bar-row");
		Span name = new Span(label);
		name.addClassName("bar-label");
		Div track = new Div();
		track.addClassName("bar-track");
		Div fill = new Div();
		fill.addClassName("bar-fill");
		// Scale the bar to the largest count so the leader fills the track; floor at 2%
		// so even a single hit is visible. The value label carries the exact number.
		long width = Math.max(2, Math.round(100.0 * count.count() / max));
		fill.getStyle().set("width", width + "%");
		track.add(fill);
		Span value = new Span(formatCount(count.count()) + percent(count.share()));
		value.addClassNames("bar-value", "mono");
		row.add(name, track, value);
		return row;
	}

	private Component emptyState() {
		VerticalLayout empty = new VerticalLayout();
		empty.setAlignItems(FlexComponent.Alignment.CENTER);
		empty.getStyle().set("padding", "var(--vaadin-padding-xl, 2rem)");
		Span message = new Span("No projects generated yet — go make one!");
		empty.add(message, new RouterLink("Open the generator", MainView.class));
		return empty;
	}

	private void applyTheme(String theme) {
		setDarkMode(ThemeSupport.isDark(theme));
	}

	private void toggleTheme() {
		setDarkMode(!this.darkMode);
		this.preferences.saveTheme(this.darkMode ? "dark" : "light");
	}

	private void setDarkMode(boolean dark) {
		this.darkMode = dark;
		this.themeToggle.setIcon(new Icon(dark ? VaadinIcon.SUN : VaadinIcon.MOON));
		String label = dark ? "Switch to light mode" : "Switch to dark mode";
		this.themeToggle.setAriaLabel(label);
		this.themeToggle.setTooltipText(label);
		ThemeSupport.applyColorScheme(this, dark);
	}

	private static String formatCount(long value) {
		return String.format(Locale.US, "%,d", value);
	}

	private static String megabytes(long bytes) {
		long mb = Math.round(bytes / 1_048_576.0);
		return (mb >= 1024) ? String.format(Locale.US, "%.1f GB", mb / 1024.0) : mb + " MB";
	}

	private static String duration(Duration duration) {
		long days = duration.toDays();
		if (days > 0) {
			return days + " d " + duration.toHoursPart() + " h";
		}
		long hours = duration.toHours();
		if (hours > 0) {
			return hours + " h " + duration.toMinutesPart() + " min";
		}
		return duration.toMinutes() + " min";
	}

	private static String percent(double share) {
		return (share > 0) ? " · " + Math.round(share * 100) + "%" : "";
	}

	private static String relative(Instant instant) {
		if (instant == null) {
			return "—";
		}
		Duration elapsed = Duration.between(instant, Instant.now());
		long minutes = elapsed.toMinutes();
		if (minutes < 1) {
			return "just now";
		}
		if (minutes < 60) {
			return minutes + " min ago";
		}
		long hours = elapsed.toHours();
		if (hours < 24) {
			return hours + " h ago";
		}
		return elapsed.toDays() + " d ago";
	}

}
