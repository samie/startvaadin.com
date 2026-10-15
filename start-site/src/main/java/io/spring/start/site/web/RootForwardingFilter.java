/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.web;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Serves the Vaadin UI at the application root while keeping {@code /} available for
 * Initializr's CLI/HAL contract. Vaadin is root-mapped, so its routes live at {@code /},
 * {@code /statistics}, etc.; but Initializr also owns {@code GET /} for the
 * {@code text/plain} and {@code *+json} metadata that {@code curl}, HTTPie and the Spring
 * CLI depend on. Content negotiation alone is not enough: the Flow client's own framework
 * requests ({@code /?v-r=init|uidl|heartbeat}) hit {@code /} without an HTML
 * {@code Accept}, so they would be mis-routed to Initializr. This filter forwards the
 * requests that belong to Vaadin — the browser's HTML page load and every {@code ?v-r=…}
 * framework request — to the Vaadin servlet, and lets the rest fall through to
 * Initializr. It mirrors what Vaadin's own {@code RootExcludeHandler} does for non-root
 * routes.
 *
 * @author Sami Ekblad
 */
public class RootForwardingFilter extends OncePerRequestFilter {

	private static final String VAADIN_SERVLET_NAME = "springServlet";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		if (isVaadinRootRequest(request)) {
			request.getServletContext().getNamedDispatcher(VAADIN_SERVLET_NAME).forward(request, response);
			return;
		}
		chain.doFilter(request, response);
	}

	private boolean isVaadinRootRequest(HttpServletRequest request) {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		if (!path.equals("/")) {
			return false;
		}
		if (request.getParameter("v-r") != null) {
			return true;
		}
		String accept = request.getHeader("Accept");
		return accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);
	}

}
