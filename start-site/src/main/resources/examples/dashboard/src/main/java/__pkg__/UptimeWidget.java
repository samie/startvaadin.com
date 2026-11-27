package {{package}};

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.signals.Signal;

import {{package}}.SystemMetrics.Sample;

/** How long the application has been running, and since when. */
public class UptimeWidget extends Card {

	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

	private final Span value = new Span();

	private final Span since = new Span();

	public UptimeWidget(Signal<List<Sample>> samples) {
		setTitle("Uptime");
		this.value.addClassName("metric-value");
		this.since.addClassName("metric-detail");
		VerticalLayout content = new VerticalLayout(this.value, this.since);
		content.setPadding(false);
		content.setSpacing(false);
		content.addClassName("metric-widget");
		add(content);

		Signal.effect(this, () -> {
			Sample now = samples.get().getLast();
			this.value.setText(format(now.uptime()));
			this.since.setText("Since " + TIME.format(now.time().minus(now.uptime()).atZone(ZoneId.systemDefault())));
		});
	}

	private static String format(Duration uptime) {
		return (uptime.toHours() > 0) ? uptime.toHours() + " h " + uptime.toMinutesPart() + " min"
				: uptime.toMinutesPart() + " min " + uptime.toSecondsPart() + " s";
	}

}
