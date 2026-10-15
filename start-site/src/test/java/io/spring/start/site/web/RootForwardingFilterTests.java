/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests the root content-negotiation handled by {@link RootForwardingFilter}: browser
 * page loads and Flow framework requests go to Vaadin; Initializr's CLI/HAL requests fall
 * through.
 *
 * @author Sami Ekblad
 */
class RootForwardingFilterTests {

	private final RootForwardingFilter filter = new RootForwardingFilter();

	private final RequestDispatcher dispatcher = mock(RequestDispatcher.class);

	private final FilterChain chain = mock(FilterChain.class);

	@Test
	void browserPageLoadIsForwardedToVaadin() throws Exception {
		MockHttpServletRequest request = rootRequest("GET");
		request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);
		assertForwardedToVaadin(request);
	}

	@Test
	void flowFrameworkRequestIsForwardedToVaadin() throws Exception {
		MockHttpServletRequest request = rootRequest("GET");
		request.addParameter("v-r", "init");
		request.addHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
		assertForwardedToVaadin(request);
	}

	@Test
	void uidlPostIsForwardedToVaadin() throws Exception {
		MockHttpServletRequest request = rootRequest("POST");
		request.addParameter("v-r", "uidl");
		assertForwardedToVaadin(request);
	}

	@ParameterizedTest
	@ValueSource(strings = { "Slackbot-LinkExpanding 1.0 (+https://api.slack.com/robots)", "Twitterbot/1.0",
			"facebookexternalhit/1.1", "LinkedInBot/1.0", "Mozilla/5.0 (compatible; Googlebot/2.1)" })
	void socialCrawlerIsForwardedToVaadin(String userAgent) throws Exception {
		MockHttpServletRequest request = rootRequest("GET");
		request.addHeader(HttpHeaders.USER_AGENT, userAgent);
		request.addHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
		assertForwardedToVaadin(request);
	}

	@Test
	void socialCrawlerOnNonRootPathFallsThrough() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(context(), "GET", "/starter.zip");
		request.addHeader(HttpHeaders.USER_AGENT, "Slackbot-LinkExpanding 1.0");
		request.addHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
		assertFallsThrough(request);
	}

	@Test
	void metadataRequestFallsThroughToInitializr() throws Exception {
		MockHttpServletRequest request = rootRequest("GET");
		request.addHeader(HttpHeaders.ACCEPT, "application/hal+json");
		assertFallsThrough(request);
	}

	@Test
	void commandLineRequestFallsThroughToInitializr() throws Exception {
		MockHttpServletRequest request = rootRequest("GET");
		request.addHeader(HttpHeaders.ACCEPT, MediaType.ALL_VALUE);
		assertFallsThrough(request);
	}

	@Test
	void nonRootPathFallsThrough() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(context(), "GET", "/starter.zip");
		request.addHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE);
		assertFallsThrough(request);
	}

	private void assertForwardedToVaadin(MockHttpServletRequest request) throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		this.filter.doFilter(request, response, this.chain);
		verify(this.dispatcher).forward(request, response);
		verify(this.chain, never()).doFilter(any(), any());
	}

	private void assertFallsThrough(MockHttpServletRequest request) throws Exception {
		MockHttpServletResponse response = new MockHttpServletResponse();
		this.filter.doFilter(request, response, this.chain);
		verify(this.chain).doFilter(request, response);
		verify(this.dispatcher, never()).forward(any(), any());
	}

	private MockHttpServletRequest rootRequest(String method) {
		return new MockHttpServletRequest(context(), method, "/");
	}

	private ServletContext context() {
		ServletContext servletContext = mock(ServletContext.class);
		given(servletContext.getNamedDispatcher("springServlet")).willReturn(this.dispatcher);
		return servletContext;
	}

}
