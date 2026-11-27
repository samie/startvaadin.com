package {{package}};

import java.util.List;
import java.util.Locale;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.progressbar.ProgressBarVariant;
import com.vaadin.flow.signals.Signal;

import {{package}}.SystemMetrics.Sample;

/** How full the heap, the physical memory and the disk are, each rated OK, High or Critical. */
public class ResourceUsageWidget extends Card {

	private final UsageBar heap = new UsageBar("Heap of max");

	private final UsageBar memory = new UsageBar("Physical memory");

	private final UsageBar disk = new UsageBar("Disk");

	public ResourceUsageWidget(Signal<List<Sample>> samples) {
		setTitle("Resources");
		VerticalLayout content = new VerticalLayout(this.heap, this.memory, this.disk);
		content.setPadding(false);
		content.addClassName("usage-list");
		add(content);

		Signal.effect(this, () -> {
			Sample now = samples.get().getLast();
			this.heap.show(now.heapUsed(), now.heapMax());
			this.memory.show(now.memoryUsed(), now.memoryTotal());
			this.disk.show(now.diskUsed(), now.diskTotal());
		});
	}

	/** A labelled bar with the percentage in use and its status. */
	private static class UsageBar extends VerticalLayout {

		private final Span percentage = new Span();

		private final Badge status = new Badge();

		private final ProgressBar bar = new ProgressBar();

		UsageBar(String resource) {
			NativeLabel label = new NativeLabel(resource);
			label.setId("usage-" + resource.toLowerCase(Locale.ROOT).replace(' ', '-'));
			HorizontalLayout caption = new HorizontalLayout(label, this.status);
			caption.addToEnd(this.percentage);
			caption.setAlignItems(Alignment.CENTER);
			caption.setWidthFull();
			// ProgressBar has no label of its own: name it after the label for screen readers
			this.bar.getElement().setAttribute("aria-labelledby", label.getId().orElseThrow());
			this.status.addThemeVariants(BadgeVariant.SMALL);
			this.percentage.addClassName("usage-percentage");
			add(caption, this.bar);
			addClassName("usage-bar");
			setPadding(false);
			setSpacing(false);
		}

		void show(long used, long total) {
			double ratio = (total > 0) ? (double) used / total : 0;
			boolean high = ratio >= 0.7;
			boolean critical = ratio >= 0.9;
			this.bar.setValue(ratio);
			this.bar.setThemeVariant(ProgressBarVariant.SUCCESS, !high);
			this.bar.setThemeVariant(ProgressBarVariant.ERROR, critical);
			this.percentage.setText(Math.round(ratio * 100) + "%");
			this.status.setText(critical ? "Critical" : high ? "High" : "OK");
			this.status.setThemeVariant(BadgeVariant.SUCCESS, !high);
			this.status.setThemeVariant(BadgeVariant.WARNING, high && !critical);
			this.status.setThemeVariant(BadgeVariant.ERROR, critical);
		}

	}

}
