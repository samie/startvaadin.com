/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Owns {@code GET /} for {@code text/html} requests so the Vaadin UI is served to
 * browsers. Initializr's {@code ProjectMetadataController} continues to own the JSON /
 * HAL variants of the same path (the curl/HTTPie contract documented in
 * {@code USING.adoc}), and Spring MVC's content negotiation routes each request to the
 * right handler. A redirect (not a forward) is used so the browser URL updates to
 * {@code /ui/} — that way Vaadin's host page resolves its relative resource paths (e.g.
 * {@code ./VAADIN/build/*.js}) correctly. The browser preserves the {@code #!}-share
 * fragment across the redirect, so existing share links still work.
 *
 * @author Vaadin UI Migration
 */
@Controller
public class HomeController {

	@GetMapping(path = "/", produces = MediaType.TEXT_HTML_VALUE)
	public String home() {
		return "redirect:/ui/";
	}

	// Friendly aliases for the statistics page. Unlike "/", these paths are not part of
	// Initializr's CLI/HAL contract, so they redirect unconditionally to the Vaadin view.
	@GetMapping({ "/stats", "/stats/", "/statistics", "/statistics/" })
	public String statistics() {
		return "redirect:/ui/statistics";
	}

}
