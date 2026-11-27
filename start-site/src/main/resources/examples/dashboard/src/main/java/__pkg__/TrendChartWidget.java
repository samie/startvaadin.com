package {{package}};

import java.time.Duration;
import java.util.List;
import java.util.function.ToDoubleFunction;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.signals.Signal;
import org.vaadin.svgvis.SvgSparkLine;
import org.vaadin.svgvis.SvgSparkLine.DataPoint;

import {{package}}.SystemMetrics.Sample;

/**
 * Two series of the samples shown, on a shared scale. The x axis spans the samples, so
 * the chart fills its width from the first sample on, and the subtitle says how long a
 * time that is. The legend badges take the series' colours from styles.css.
 */
public class TrendChartWidget extends Card {

	/** A named line of the chart. */
	public record Series(String name, ToDoubleFunction<Sample> value) {
	}

	private final SvgSparkLine chart = new SvgSparkLine(160);

	public TrendChartWidget(String title, String unit, Signal<List<Sample>> samples, Series line, Series series) {
		setTitle(title);
		Badge lineLegend = new Badge(line.name());
		lineLegend.addClassName("aura-accent-blue");
		Badge seriesLegend = new Badge(series.name());
		seriesLegend.addClassName("aura-accent-purple");
		setHeaderSuffix(new HorizontalLayout(lineLegend, seriesLegend));
		this.chart.setUnit(unit);
		// Straight segments: a curve would overshoot the real minimum and maximum
		this.chart.setUseBezierCurve(false);
		this.chart.addClassName("trend-chart");
		add(this.chart);

		Signal.effect(this, () -> {
			List<Sample> history = samples.get();
			Duration span = Duration.between(history.getFirst().time(), history.getLast().time());
			setSubtitle("Last " + format(span));
			this.chart.setData(points(history, line));
			this.chart.addSeries(points(history, series), null);
		});
	}

	private static List<DataPoint> points(List<Sample> history, Series series) {
		return history.stream()
			.map((sample) -> DataPoint.of(sample.time(), series.value().applyAsDouble(sample)))
			.toList();
	}

	private static String format(Duration span) {
		return (span.toMinutes() > 0) ? span.toMinutes() + " min" : span.toSeconds() + " s";
	}

}
