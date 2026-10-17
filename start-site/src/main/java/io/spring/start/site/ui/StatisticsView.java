/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.shared.Registration;
import io.spring.start.site.StartConfigurationProperties;
import io.spring.start.site.stats.Count;
import io.spring.start.site.stats.StatisticsService;

/**
 * A light-hearted usage dashboard for the curious. Served at {@code /statistics} (plus a
 * {@code /stats} alias) by Vaadin's root mapping. Mirrors {@link MainView}'s shell (Aura
 * theme + dark-mode toggle) and refreshes itself on a poll so the total ticks up live
 * while the page is watched.
 *
 * @author Sami Ekblad
 */
@Route("statistics")
@RouteAlias("stats")
@PageTitle("Statistics · " + Branding.NAME)
public final class StatisticsView extends AppLayout {

	private static final int TOP_DEPENDENCIES = 12;

	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US)
		.withZone(ZoneId.systemDefault());

	private final StatisticsService statistics;

	private final InitializrUiService service;

	private final Preferences preferences;

	private final int pollMillis;

	private final Button themeToggle;

	private final VerticalLayout content;

	private boolean darkMode = true;

	private Registration pollRegistration;

	public StatisticsView(StatisticsService statistics, InitializrUiService service, Preferences preferences,
			StartConfigurationProperties properties) {
		this.statistics = statistics;
		this.service = service;
		this.preferences = preferences;
		this.pollMillis = Math.max(2, properties.getStatistics().getPollIntervalSeconds()) * 1000;
		this.themeToggle = new Button(new Icon(VaadinIcon.SUN_O), (event) -> toggleTheme());
		this.themeToggle.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
		this.themeToggle.setAriaLabel("Switch to light mode");
		this.themeToggle.setTooltipText("Switch to light mode");
		buildNavbar();
		this.content = new VerticalLayout();
		this.content.addClassName("stats-content");
		setContent(this.content);
		refresh();
	}

	private void buildNavbar() {
		RouterLink back = new RouterLink("← Generator", MainView.class);
		back.getStyle().set("font-size", "0.875rem").set("margin-left", "var(--lumo-space-m)");
		H1 title = new H1("Statistics");
		title.getStyle().set("font-size", "1.125rem").set("margin", "0 0 0 var(--lumo-space-m)");
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
		this.content.removeAll();
		long total = this.statistics.total();
		if (total == 0) {
			this.content.add(emptyState());
			return;
		}
		this.content.add(buildTiles(total));
		this.content.add(section("Top dependencies", bars(this.statistics.topDependencies(TOP_DEPENDENCIES), true)));
		this.content.add(section("Spring Boot versions", bars(this.statistics.bootVersionDistribution(), false)));
		this.content.add(section("Java versions", bars(this.statistics.javaVersionDistribution(), false)));
		this.content.add(section("Build systems", bars(this.statistics.buildSystemDistribution(), false)));
		this.content.add(section("Languages", bars(this.statistics.languageDistribution(), false)));
		this.content.add(section("Packaging", bars(this.statistics.packagingDistribution(), false)));
		this.content.add(section("Generate vs Explore", bars(this.statistics.uiActionDistribution(), false)));
		this.content.add(section("Arrived from", bars(this.statistics.referrerDistribution(), false)));
	}

	private Component buildTiles(long total) {
		FlexLayout tiles = new FlexLayout();
		tiles.addClassName("stat-tiles");
		tiles.add(tile("Projects generated", formatCount(total)));
		tiles.add(tile("Counting since", DATE.format(this.statistics.countingSince())));
		tiles.add(tile("Average / day", formatCount(Math.round(this.statistics.perDayAverage()))));
		tiles.add(tile("Last generated", relative(this.statistics.lastGenerated())));
		return tiles;
	}

	private static Component tile(String label, String value) {
		Div tile = new Div();
		tile.addClassNames("stat-tile", "aura-surface");
		Div number = new Div();
		number.addClassNames("stat-number", "mono");
		number.setText(value);
		Div caption = new Div();
		caption.addClassName("stat-label");
		caption.setText(label);
		tile.add(number, caption);
		return tile;
	}

	private static Component section(String title, Component body) {
		VerticalLayout section = new VerticalLayout();
		section.setPadding(false);
		section.setSpacing(false);
		H2 heading = new H2(title);
		heading.getStyle().set("font-size", "1rem").set("margin", "var(--lumo-space-m) 0 var(--lumo-space-xs)");
		section.add(heading, body);
		return section;
	}

	private Component bars(List<Count> counts, boolean resolveDependencyNames) {
		if (counts.isEmpty()) {
			Span none = new Span("No data yet.");
			none.getStyle().set("color", "var(--lumo-secondary-text-color)");
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
		empty.getStyle().set("padding", "var(--lumo-space-xl)");
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
		this.themeToggle.setIcon(new Icon(dark ? VaadinIcon.SUN_O : VaadinIcon.MOON));
		String label = dark ? "Switch to light mode" : "Switch to dark mode";
		this.themeToggle.setAriaLabel(label);
		this.themeToggle.setTooltipText(label);
		ThemeSupport.applyColorScheme(this, dark);
	}

	private static String formatCount(long value) {
		return String.format(Locale.US, "%,d", value);
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
