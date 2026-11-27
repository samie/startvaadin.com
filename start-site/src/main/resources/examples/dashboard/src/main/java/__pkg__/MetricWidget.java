package {{package}};

import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.ToDoubleFunction;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.signals.Signal;
import org.vaadin.svgvis.SvgSparkLine;

import {{package}}.SystemMetrics.Sample;

/**
 * A key figure: the latest value of one metric, its change over the last minute and a
 * sparkline of the samples shown.
 */
public class MetricWidget extends Card {

	private static final int MINUTE_OF_SAMPLES = 30;

	private final Span value = new Span();

	private final Badge trend = new Badge();

	private final SvgSparkLine sparkline = new SvgSparkLine(50);

	public MetricWidget(String title, Signal<List<Sample>> samples, ToDoubleFunction<Sample> metric,
			DoubleFunction<String> format) {
		setTitle(title);
		setHeaderSuffix(this.trend);
		this.value.addClassName("metric-value");
		this.sparkline.setUseBezierCurve(false);
		VerticalLayout content = new VerticalLayout(this.value, this.sparkline);
		content.setPadding(false);
		content.setSpacing(false);
		content.addClassName("metric-widget");
		add(content);

		Signal.effect(this, () -> {
			List<Sample> history = samples.get();
			double now = metric.applyAsDouble(history.getLast());
			double minuteAgo = metric.applyAsDouble(history.get(Math.max(0, history.size() - 1 - MINUTE_OF_SAMPLES)));
			this.value.setText(format.apply(now));
			String change = format.apply(Math.abs(now - minuteAgo));
			// No arrow for a change too small to show
			this.trend.setText(change.equals(format.apply(0)) ? change : arrow(now - minuteAgo) + change);
			this.sparkline.setData(history.stream().mapToDouble(metric).toArray());
		});
	}

	private static String arrow(double change) {
		return (change > 0) ? "▲ " : "▼ ";
	}

}
