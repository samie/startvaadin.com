/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui.examples;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TemplateLinkController}.
 *
 * @author Sami Ekblad
 */
class TemplateLinkControllerTests {

	private final TemplateLinkController controller = new TemplateLinkController();

	@Test
	void knownTemplateRedirectsToExampleIdShareLink() {
		assertThat(this.controller.template("crud")).isEqualTo("redirect:/#!exampleId=crud");
	}

	@Test
	void unknownTemplateRedirectsToRoot() {
		assertThat(this.controller.template("nope")).isEqualTo("redirect:/");
		assertThat(this.controller.template("crud&javaVersion=8")).isEqualTo("redirect:/");
	}

	@Test
	void linkPathMatchesControllerMapping() {
		assertThat(ExampleCatalog.find("mobile").linkPath()).isEqualTo("/template/mobile");
	}

}
