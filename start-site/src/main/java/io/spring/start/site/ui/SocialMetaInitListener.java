/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import io.spring.start.site.StartConfigurationProperties;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Element;

/**
 * Injects social sharing and SEO metadata into the Vaadin host page: a meta description,
 * Open Graph and Twitter card tags, a canonical link, favicon/manifest links and
 * schema.org JSON-LD. Static assets are served from the server root, so asset links are
 * root-relative; canonical and {@code og:url} are absolute, built from the configured
 * public URL (see {@link StartConfigurationProperties#getPublicUrl()}).
 *
 * @author Sami Ekblad
 */
public class SocialMetaInitListener implements VaadinServiceInitListener {

	private static final String IMAGE_PATH = "/images/social-card.png";

	private static final String THEME_COLOR = "#056ff0";

	private final String baseUrl;

	public SocialMetaInitListener(StartConfigurationProperties properties) {
		this.baseUrl = properties.getPublicUrl().replaceAll("/+$", "");
	}

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.addIndexHtmlRequestListener(this::addTags);
	}

	private void addTags(IndexHtmlResponse response) {
		VaadinRequest request = response.getVaadinRequest();
		String path = (request != null) ? request.getPathInfo() : null;
		String pageUrl = this.baseUrl + ((path == null || path.equals("/")) ? "" : path);
		applyMetaTags(response.getDocument().head(), pageUrl, this.baseUrl);
	}

	/**
	 * Appends the social/SEO tags to the given document head. Package-private and static
	 * so it can be exercised without the Vaadin request plumbing.
	 * @param head the host page {@code <head>} element
	 * @param pageUrl absolute canonical URL of the requested page
	 * @param baseUrl absolute public base URL (no trailing slash)
	 */
	static void applyMetaTags(Element head, String pageUrl, String baseUrl) {
		String image = baseUrl + IMAGE_PATH;

		// Static <title> fallback: Flow sets the per-route title via JS at runtime, but
		// non-JS crawlers and link scrapers only see what is in the initial HTML.
		Element title = head.selectFirst("title");
		if (title == null) {
			title = head.appendElement("title");
		}
		title.text(Branding.TITLE);

		addMeta(head, "name", "description", Branding.DESCRIPTION);

		addMeta(head, "property", "og:type", "website");
		addMeta(head, "property", "og:site_name", Branding.NAME);
		addMeta(head, "property", "og:title", Branding.TITLE);
		addMeta(head, "property", "og:description", Branding.DESCRIPTION);
		addMeta(head, "property", "og:url", pageUrl);
		addMeta(head, "property", "og:image", image);
		addMeta(head, "property", "og:image:width", "1200");
		addMeta(head, "property", "og:image:height", "630");
		addMeta(head, "property", "og:image:alt", Branding.IMAGE_ALT);

		addMeta(head, "name", "twitter:card", "summary_large_image");
		addMeta(head, "name", "twitter:title", Branding.TITLE);
		addMeta(head, "name", "twitter:description", Branding.DESCRIPTION);
		addMeta(head, "name", "twitter:image", image);

		addMeta(head, "name", "theme-color", THEME_COLOR);

		head.appendElement("link").attr("rel", "canonical").attr("href", pageUrl);
		head.appendElement("link").attr("rel", "icon").attr("type", "image/svg+xml").attr("href", "/favicon.svg");
		head.appendElement("link")
			.attr("rel", "icon")
			.attr("type", "image/png")
			.attr("sizes", "32x32")
			.attr("href", "/images/icon-32.png");
		head.appendElement("link").attr("rel", "apple-touch-icon").attr("href", "/images/apple-touch-icon.png");
		head.appendElement("link").attr("rel", "manifest").attr("href", "/site.webmanifest");

		Element jsonLd = head.appendElement("script").attr("type", "application/ld+json");
		jsonLd.appendChild(new DataNode(structuredData(baseUrl)));
	}

	private static String structuredData(String baseUrl) {
		return """
				{"@context":"https://schema.org","@type":"WebApplication",\
				"name":"%s","url":"%s","description":"%s",\
				"applicationCategory":"DeveloperApplication","operatingSystem":"Any",\
				"offers":{"@type":"Offer","price":"0","priceCurrency":"USD"}}""".formatted(escape(Branding.NAME),
				escape(baseUrl), escape(Branding.DESCRIPTION));
	}

	private static void addMeta(Element head, String keyAttr, String key, String content) {
		head.appendElement("meta").attr(keyAttr, key).attr("content", content);
	}

	private static String escape(String value) {
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

}
