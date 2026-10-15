/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import java.net.URI;

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
	static final String TAGLINE = "Create Vaadin UI for your Java project.";

	/** Meta/Open Graph description (~155 chars). */
	static final String DESCRIPTION = "Generate a ready-to-run Spring Boot project with a Vaadin front end. "
			+ "Choose your dependencies, build tool and language, then download or explore the code.";

	/** Alternative text for the social card image. */
	static final String IMAGE_ALT = NAME;

	/** Vaadin quickstart tutorial, linked from the drawer and the navbar. */
	static final String TUTORIAL_BASE_URL = "https://vaadin.com/quickstart";

	/** Source repository for this site, linked from the drawer. */
	static final String REPOSITORY_URL = "https://github.com/samie/startvaadin.com";

	private Branding() {
	}

	/**
	 * Builds the tutorial link tagged with the site's own public host as
	 * {@code utm_source}, so analytics reflect the real URL the visitor was on rather
	 * than a guessed/hardcoded one.
	 * @param publicUrl the deployment's public base URL (see
	 * {@code StartConfigurationProperties#getPublicUrl()})
	 * @return the tutorial URL with {@code utm_source} set to the public URL's host
	 */
	static String tutorialUrl(String publicUrl) {
		String host = URI.create(publicUrl).getHost();
		return TUTORIAL_BASE_URL + "?utm_source=" + host;
	}

}
