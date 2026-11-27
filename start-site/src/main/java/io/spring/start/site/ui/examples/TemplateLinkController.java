/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui.examples;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Clean, typeable links to a UI template: {@code /template/<id>} redirects to the
 * {@code #!exampleId=<id>} share link that the main view applies. Being a path (not a
 * fragment), editing it in the address bar always reloads the page. Unknown ids land on
 * the plain generator.
 *
 * @author Sami Ekblad
 */
@Controller
public class TemplateLinkController {

	@GetMapping("/template/{id}")
	public String template(@PathVariable String id) {
		Example example = ExampleCatalog.find(id);
		// Redirect with the catalog's id, never the raw path segment.
		return (example != null) ? "redirect:/#!exampleId=" + example.id() : "redirect:/";
	}

}
