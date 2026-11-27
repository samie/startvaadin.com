package {{package}};

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.vaadin.browserless.BrowserlessApplicationContext;
import com.vaadin.browserless.BrowserlessUIContext;
import com.vaadin.browserless.SpringBrowserlessApplicationContext;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.DescriptionList.Description;
import com.vaadin.flow.component.html.DescriptionList.Term;
import com.vaadin.flow.component.html.Span;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.vaadin.svgvis.SvgSparkLine;

import {{package}}.SystemMetrics.Sample;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The dashboard's use cases, with the scheduled sampling switched off: each test publishes
 * the samples it needs. That the widgets and charts render in a browser is checked by the
 * smoke test.
 */
@SpringBootTest(properties = "dashboard.sampling=-")
class DashboardViewTest {

	private static final long GB = 1024 * 1024 * 1024;

	static {
		// svg-visualizations 1.2.0 sets SVG's case-sensitive viewBox attribute and draws while
		// being attached. Both trip consistency checks in Flow that run only with assertions
		// on, as in tests. Remove once the add-on avoids them.
		ClassLoader.getSystemClassLoader().setPackageAssertionStatus("com.vaadin.flow.dom", false);
	}

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private SystemMetrics metrics;

	private BrowserlessApplicationContext app;

	private BrowserlessUIContext window;

	@BeforeEach
	void openDashboard() {
		this.app = SpringBrowserlessApplicationContext.create(this.applicationContext, DashboardView.class);
		this.window = this.app.newUser().newWindow();
	}

	@AfterEach
	void close() {
		this.app.close();
	}

	@Test
	void dashboardIsHeadedJvmDashboard() {
		assertEquals("JVM Dashboard", this.window.findH1().component().getText());
	}

	@Test
	void userSeesTheKeyFiguresOfTheLatestSample() {
		publish(sample(0.25, 42, 0.5));

		assertEquals("25%", figureOf("CPU load"));
		assertEquals("512 MB", figureOf("Heap used"));
		assertEquals("42", figureOf("Live threads"));
		assertEquals("1 h 2 min", figureOf("Uptime"));
	}

	@Test
	void keyFiguresShowTheirChangeOverTheLastMinute() {
		for (int i = 0; i < 30; i++) {
			publish(sample(0.25, 42, 0.5));
		}
		publish(sample(0.6, 40, 0.5));

		assertEquals("60%", figureOf("CPU load"));
		assertEquals("▲ 35%", trendOf("CPU load"));
		assertEquals("▼ 2", trendOf("Live threads"));
	}

	@Test
	void pausingFreezesTheDashboardUntilResumed() {
		publish(sample(0.25, 42, 0.5));

		this.window.findSwitch().withLabel("Pause").switchOn();
		publish(sample(0.6, 42, 0.5));
		assertEquals("25%", figureOf("CPU load"));

		this.window.findSwitch().withLabel("Pause").switchOff();
		assertEquals("60%", figureOf("CPU load"));
	}

	@Test
	void resourceUsageIsRatedOkHighOrCritical() {
		publish(new Sample(Instant.now(), 0.1, 0.05, GB / 2, GB, GB, 12 * GB, 16 * GB, 95 * GB, 100 * GB, 20,
				Duration.ofMinutes(5), 3));

		assertEquals("50% OK", usageOf("Heap of max"));
		assertEquals("75% High", usageOf("Physical memory"));
		assertEquals("95% Critical", usageOf("Disk"));
	}

	@Test
	void userSeesTrendChartsOfCpuAndHeap() {
		publish(sample(0.25, 42, 0.5));

		assertTrue(this.window.find(SvgSparkLine.class, widget("CPU trend")).exists());
		assertTrue(this.window.find(SvgSparkLine.class, widget("Heap trend")).exists());
	}

	@Test
	void userSeesHowOftenTheJvmHasCollectedGarbage() {
		publish(sample(0.25, 42, 0.5));

		assertEquals("7", factOf("GC collections"));
	}

	@Test
	void dashboardShowsEightWidgets() {
		assertEquals(List.of("CPU load", "Heap used", "Live threads", "Uptime", "CPU trend", "Heap trend", "Resources",
				"System"), this.window.findCard().components().stream().map(Card::getTitleAsText).toList());
	}

	/** Publishes a sample as the scheduled sampling would. */
	private void publish(Sample sample) {
		this.metrics.publish(sample);
		this.window.runPendingSignalsTasks();
	}

	private static Sample sample(double cpu, int threads, double heapOfMax) {
		return new Sample(Instant.now(), cpu, cpu / 2, (long) (heapOfMax * GB), GB, GB, 8 * GB, 16 * GB, 10 * GB,
				100 * GB, threads, Duration.ofMinutes(62), 7);
	}

	private Card widget(String title) {
		return this.window.findCard().withCondition((card) -> title.equals(card.getTitleAsText())).component();
	}

	/** The large figure in a key-figure widget. */
	private String figureOf(String title) {
		return this.window.find(Span.class, widget(title)).withClassName("metric-value").single().getText();
	}

	private String trendOf(String title) {
		return ((Badge) widget(title).getHeaderSuffix()).getText();
	}

	/** The percentage and status shown next to a resource's label. */
	private String usageOf(String resource) {
		Component caption = this.window.findNativeLabel().withText(resource).component().getParent().orElseThrow();
		return this.window.findSpan().inside(caption).getText() + " "
				+ this.window.find(Badge.class, caption).single().getText();
	}

	/** The value next to a term in the System widget. */
	private String factOf(String term) {
		Term label = this.window.find(Term.class, widget("System")).withText(term).single();
		List<Component> facts = label.getParent().orElseThrow().getChildren().toList();
		return ((Description) facts.get(facts.indexOf(label) + 1)).getText();
	}

}
