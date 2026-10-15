/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SocialMetaInitListener} head mutation, exercised directly against a
 * jsoup document so no Vaadin request plumbing or TestBench license is required.
 *
 * @author Sami Ekblad
 */
class SocialMetaInitListenerTests {

	private static final String BASE = "https://pro.startvaadin.com";

	private Element head;

	@BeforeEach
	void setUp() {
		Document document = Jsoup.parse("<html><head></head><body></body></html>");
		this.head = document.head();
		SocialMetaInitListener.applyMetaTags(this.head, BASE + "/statistics", BASE);
	}

	@Test
	void setsStaticTitleFallback() {
		assertThat(this.head.selectFirst("title").text()).isEqualTo(Branding.TITLE);
	}

	@Test
	void addsDescriptionAndOpenGraphTags() {
		assertThat(meta("name", "description")).isEqualTo(Branding.DESCRIPTION);
		assertThat(meta("property", "og:title")).isEqualTo(Branding.TITLE);
		assertThat(meta("property", "og:description")).isEqualTo(Branding.DESCRIPTION);
		assertThat(meta("property", "og:type")).isEqualTo("website");
		assertThat(meta("property", "og:url")).isEqualTo(BASE + "/statistics");
		assertThat(meta("property", "og:image")).isEqualTo(BASE + "/images/social-card.png");
		assertThat(meta("property", "og:image:width")).isEqualTo("1200");
		assertThat(meta("property", "og:image:height")).isEqualTo("630");
	}

	@Test
	void addsTwitterCardTags() {
		assertThat(meta("name", "twitter:card")).isEqualTo("summary_large_image");
		assertThat(meta("name", "twitter:title")).isEqualTo(Branding.TITLE);
		assertThat(meta("name", "twitter:image")).isEqualTo(BASE + "/images/social-card.png");
	}

	@Test
	void addsCanonicalFaviconAndManifestLinks() {
		assertThat(this.head.selectFirst("link[rel=canonical]").attr("href")).isEqualTo(BASE + "/statistics");
		assertThat(this.head.selectFirst("link[rel=icon][type=image/svg+xml]").attr("href")).isEqualTo("/favicon.svg");
		assertThat(this.head.selectFirst("link[rel=apple-touch-icon]").attr("href"))
			.isEqualTo("/images/apple-touch-icon.png");
		assertThat(this.head.selectFirst("link[rel=manifest]").attr("href")).isEqualTo("/site.webmanifest");
		assertThat(meta("name", "theme-color")).isEqualTo("#056ff0");
	}

	@Test
	void addsStructuredData() {
		Element script = this.head.selectFirst("script[type=application/ld+json]");
		assertThat(script).isNotNull();
		assertThat(script.data()).contains("\"@type\":\"WebApplication\"")
			.contains("\"url\":\"" + BASE + "\"")
			.contains("DeveloperApplication");
	}

	private String meta(String keyAttr, String key) {
		Element element = this.head.selectFirst("meta[" + keyAttr + "=" + key + "]");
		return (element != null) ? element.attr("content") : null;
	}

}
