/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.support;

import java.util.List;

import io.spring.initializr.generator.version.Version;
import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;
import io.spring.initializr.web.support.SpringIoInitializrMetadataUpdateStrategy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * An {@link InitializrMetadataUpdateStrategy} that performs additional filtering of
 * versions available on spring.io.
 *
 * @author Stephane Nicoll
 * @author Moritz Halbritter
 * @author Eddú Meléndez
 */
public class StartInitializrMetadataUpdateStrategy extends SpringIoInitializrMetadataUpdateStrategy {

	private static final Version MINIMUM_BOOT_VERSION = Version.parse("4.0.0");

	private static final Log logger = LogFactory.getLog(StartInitializrMetadataUpdateStrategy.class);

	public StartInitializrMetadataUpdateStrategy(RestTemplate restTemplate, JsonMapper jsonMapper) {
		super(restTemplate, jsonMapper);
	}

	@Override
	public InitializrMetadata update(InitializrMetadata current) {
		try {
			return super.update(current);
		}
		catch (RestClientException ex) {
			// A transient failure reaching spring.io (e.g. connection reset) must not
			// break
			// metadata access; fall back to existing metadata until the next refresh.
			logger.warn("Could not refresh Spring Boot versions from spring.io; using existing metadata. Reason: "
					+ ex.getMessage());
			return current;
		}
	}

	@Override
	protected List<DefaultMetadataElement> fetchSpringBootVersions(String url) {
		List<DefaultMetadataElement> versions = super.fetchSpringBootVersions(url);
		return (versions != null) ? versions.stream().filter(this::isCompatibleVersion).toList() : null;
	}

	private boolean isCompatibleVersion(DefaultMetadataElement versionMetadata) {
		Version version = Version.parse(versionMetadata.getId());
		return version.compareTo(MINIMUM_BOOT_VERSION) >= 0;
	}

}
