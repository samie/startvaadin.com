/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui.examples;

import java.util.List;

/**
 * A selectable UI example ("starting point") whose source files are overlaid onto the
 * generated project and whose dependencies are auto-added.
 *
 * @param id stable example id, also the resource folder and screenshot name
 * @param title display title
 * @param description short description
 * @param dependencies catalog dependency ids this example requires
 * @author Sami Ekblad
 */
public record Example(String id, String title, String description, List<String> dependencies) {

	public Example {
		dependencies = List.copyOf(dependencies);
	}

	/**
	 * Classpath folder holding this example's resource tree.
	 * @return resource base path ending with {@code /}
	 */
	public String resourceBase() {
		return "examples/" + this.id + "/";
	}

	/**
	 * Site-relative path of the clean link to this example, served by
	 * {@link TemplateLinkController}.
	 * @return the link path, e.g. {@code /template/crud}
	 */
	public String linkPath() {
		return "/template/" + this.id;
	}

	/**
	 * URL of this example's screenshot, served from {@code META-INF/resources} at the
	 * context root. An absolute path is used (not a {@code context://} prefix, which
	 * Vaadin does not resolve for a runtime {@code Image} {@code src}).
	 * @return the screenshot image URL
	 */
	public String screenshotUrl() {
		return "/images/examples/" + this.id + ".png";
	}

}
