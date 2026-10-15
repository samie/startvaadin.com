/*
 * Copyright 2012 - present the original author or authors.
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
			new Example("crud", "CRUD App", "Grid and editor backed by JPA and an in-memory database.",
					List.of("data-jpa", "h2")),
			new Example("dashboard", "Dashboard", "Stat cards and progress bars on a responsive layout.", List.of()),
			new Example("mobile", "Mobile App", "Mobile-first layout with tab navigation, installable as a PWA.",
					List.of()));

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
