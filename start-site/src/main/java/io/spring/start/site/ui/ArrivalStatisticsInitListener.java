/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.communication.IndexHtmlResponse;
import io.spring.start.site.stats.StatisticsService;

/**
 * Records page arrivals by referrer into {@link StatisticsService}. Hooks the host page
 * request (which fires on a full page load, i.e. a genuine arrival, not an in-app Flow
 * navigation) and tallies the browser-supplied {@code Referer} header. Only the referrer
 * host is stored; no cookies, no full URLs, no third party.
 *
 * @author Sami Ekblad
 */
public class ArrivalStatisticsInitListener implements VaadinServiceInitListener {

	private final StatisticsService statistics;

	public ArrivalStatisticsInitListener(StatisticsService statistics) {
		this.statistics = statistics;
	}

	@Override
	public void serviceInit(ServiceInitEvent event) {
		event.addIndexHtmlRequestListener(this::recordArrival);
	}

	private void recordArrival(IndexHtmlResponse response) {
		VaadinRequest request = response.getVaadinRequest();
		if (request == null) {
			return;
		}
		this.statistics.recordArrival(request.getHeader("Referer"), selfHost(request.getHeader("Host")));
	}

	// The Host header is the domain the visitor is actually on; strip any ":port" so it
	// compares cleanly against a referrer URL's host (which never carries the port).
	private static String selfHost(String hostHeader) {
		if (hostHeader == null) {
			return null;
		}
		int colon = hostHeader.indexOf(':');
		return (colon >= 0) ? hostHeader.substring(0, colon) : hostHeader;
	}

}
