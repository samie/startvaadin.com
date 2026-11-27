/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui.examples;

import java.util.List;

/**
 * The ordered set of UI examples offered as project starting points.
 *
 * @author Sami Ekblad
 */
public final class ExampleCatalog {

	private static final List<Example> EXAMPLES = List.of(
			new Example("helloworld", "Hello World", "A view with a text field and a button.", List.of()),
			new Example("crud", "CRUD App",
					"Master-detail contact list with a validated editor, backed by JPA and an in-memory database.",
					List.of("data-jpa", "h2", "validation")),
			new Example("dashboard", "JVM Dashboard",
					"Live JVM metrics delivered with Push, with trend charts and status gauges.",
					List.of("svg-visualizations")),
			new Example("mobile", "Mobile App",
					"Bottom tabs, drill-down views and push notifications, installable as a PWA.",
					List.of("vaadin-webpush")));

	private ExampleCatalog() {
	}

	public static List<Example> all() {
		return EXAMPLES;
	}

	public static Example find(String id) {
		if (id == null) {
			return null;
		}
		return EXAMPLES.stream().filter((example) -> example.id().equals(id)).findFirst().orElse(null);
	}

}
