/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

/**
 * Shared branding and social/SEO copy. Kept in one place so the page title, the visible
 * headline and the social metadata ({@link SocialMetaInitListener}) stay in sync.
 *
 * @author Sami Ekblad
 */
final class Branding {

	/** Product name, used for the title and the navbar headline. */
	static final String NAME = "Vaadin Initializr";

	/** Full title used for the document title and social cards. */
	static final String TITLE = "Vaadin Initializr — Spring Boot + Vaadin project starter";

	/** Short, visible headline shown under the navbar title. */
	static final String TAGLINE = "Bootstrap a Spring Boot project with a Vaadin UI in seconds.";

	/** Meta/Open Graph description (~155 chars). */
	static final String DESCRIPTION = "Generate a ready-to-run Spring Boot project with a Vaadin front end. "
			+ "Choose your dependencies, build tool and language, then download or explore the code.";

	/** Alternative text for the social card image. */
	static final String IMAGE_ALT = NAME;

	private Branding() {
	}

}
