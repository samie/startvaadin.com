package {{package}};

import java.util.List;

import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.DescriptionList;
import com.vaadin.flow.component.html.DescriptionList.Description;
import com.vaadin.flow.component.html.DescriptionList.Term;
import com.vaadin.flow.signals.Signal;

import {{package}}.SystemMetrics.Sample;

/** The Java runtime the application runs on, and how often it has collected garbage. */
public class SystemInfoWidget extends Card {

	private final Description gcCollections = new Description();

	public SystemInfoWidget(Signal<List<Sample>> samples) {
		setTitle("System");
		DescriptionList facts = new DescriptionList();
		facts.add(new Term("Java"), new Description(Runtime.version().toString()));
		facts.add(new Term("Vendor"), new Description(System.getProperty("java.vendor")));
		facts.add(new Term("Processors"), new Description(String.valueOf(Runtime.getRuntime().availableProcessors())));
		facts.add(new Term("GC collections"), this.gcCollections);
		facts.addClassName("system-facts");
		add(facts);

		Signal.effect(this, () -> this.gcCollections.setText(String.valueOf(samples.get().getLast().gcCollections())));
	}

}
