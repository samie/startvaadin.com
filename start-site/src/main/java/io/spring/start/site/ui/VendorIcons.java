/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.icon.SvgIcon;
import io.spring.initializr.metadata.Dependency;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Resolves a dependency to a vendor {@link SvgIcon}. Icons are curated SVGs from
 * simple-icons, unpacked onto the classpath at build time (see start-site/pom.xml) under
 * {@code META-INF/resources/images/vendors}. Dependencies without a matching vendor icon
 * fall back to a generic Spring icon when their resolved Maven group id is
 * Spring-authored ({@code org.springframework*}), or to a generic OpenJDK icon otherwise.
 *
 * @author Sami Ekblad
 */
public final class VendorIcons {

	private static final String RESOURCE_PATTERN = "classpath*:META-INF/resources/images/vendors/*.svg";

	private static final String ICON_PATH_PREFIX = "/images/vendors/";

	private static final String ICON_EXTENSION = ".svg";

	private static final String DEFAULT_SLUG = "openjdk";

	private static final String SPRING_FALLBACK_SLUG = "spring";

	private static final String SPRING_GROUP_PREFIX = "org.springframework";

	private static final String ICON_CLASS_NAME = "dependency-icon";

	/** Dependency ids whose simple-icons slug differs from the id itself. */
	private static final Map<String, String> SLUG_OVERRIDES = Map.ofEntries(Map.entry("data-mongodb", "mongodb"),
			Map.entry("data-mongodb-reactive", "mongodb"), Map.entry("data-redis", "redis"),
			Map.entry("data-redis-reactive", "redis"), Map.entry("cassandra", "apachecassandra"),
			Map.entry("data-cassandra", "apachecassandra"), Map.entry("data-cassandra-reactive", "apachecassandra"),
			Map.entry("kafka", "apachekafka"), Map.entry("kafka-streams", "apachekafka"),
			Map.entry("data-elasticsearch", "elasticsearch"), Map.entry("data-neo4j", "neo4j"),
			Map.entry("data-couchbase", "couchbase"), Map.entry("data-couchbase-reactive", "couchbase"),
			Map.entry("rabbitmq-streams", "rabbitmq"), Map.entry("docker-compose", "docker"),
			Map.entry("dgs-codegen", "graphql"), Map.entry("netflix-dgs", "netflix"),
			Map.entry("security", "springsecurity"), Map.entry("oauth2-client", "springsecurity"),
			Map.entry("oauth2-authorization-server", "springsecurity"),
			Map.entry("oauth2-resource-server", "springsecurity"), Map.entry("security-saml2", "springsecurity"),
			Map.entry("spring-security-webauthn", "springsecurity"), Map.entry("freemarker", "apachefreemarker"),
			Map.entry("groovy-templates", "apachegroovy"), Map.entry("influx", "influxdb"),
			Map.entry("new-relic", "newrelic"), Map.entry("otlp-metrics", "opentelemetry"),
			Map.entry("cloud-starter-vault-config", "vault"), Map.entry("cloud-starter-consul-config", "consul"),
			Map.entry("cloud-starter-consul-discovery", "consul"), Map.entry("cloud-eureka", "netflix"),
			Map.entry("cloud-eureka-server", "netflix"), Map.entry("cloud-feign", "netflix"),
			Map.entry("cloud-gcp", "googlecloud"), Map.entry("cloud-gcp-pubsub", "googlepubsub"),
			Map.entry("cloud-gcp-storage", "googlecloudstorage"), Map.entry("springdoc-openapi", "swagger"),
			Map.entry("spring-ai-anthropic", "anthropic"), Map.entry("spring-ai-mistral", "mistralai"),
			Map.entry("spring-ai-ollama", "ollama"), Map.entry("spring-ai-google-genai", "googlegemini"),
			Map.entry("spring-ai-google-genai-embedding", "googlegemini"),
			Map.entry("spring-ai-elevenlabs", "elevenlabs"),
			Map.entry("spring-ai-vectordb-cassandra", "apachecassandra"),
			Map.entry("spring-ai-vectordb-couchbase", "couchbase"),
			Map.entry("spring-ai-vectordb-elasticsearch", "elasticsearch"),
			Map.entry("spring-ai-vectordb-milvus", "milvus"), Map.entry("spring-ai-vectordb-mongodb-atlas", "mongodb"),
			Map.entry("spring-ai-vectordb-neo4j", "neo4j"), Map.entry("spring-ai-vectordb-opensearch", "opensearch"),
			Map.entry("spring-ai-vectordb-aws-opensearch", "opensearch"),
			Map.entry("spring-ai-vectordb-redis", "redis"), Map.entry("spring-ai-vectordb-mariadb", "mariadb"),
			Map.entry("spring-ai-vectordb-pgvector", "postgresql"), Map.entry("spring-ai-postgresml", "postgresql"),
			Map.entry("spring-ai-vertexai-embeddings", "googlecloud"));

	private static final Set<String> AVAILABLE_SLUGS = scanAvailableSlugs();

	private VendorIcons() {
	}

	/**
	 * Builds a themeable icon for the given dependency, falling back to the Spring icon
	 * (for Spring-authored dependencies) or the default icon when no vendor-specific icon
	 * is available.
	 * @param dependency the catalog dependency (e.g. the one with id "postgresql")
	 * @return an {@link SvgIcon} with the {@code dependency-icon} CSS class applied
	 */
	public static SvgIcon icon(Dependency dependency) {
		SvgIcon icon = new SvgIcon(iconPath(resolveSlug(dependency)));
		icon.addClassName(ICON_CLASS_NAME);
		return icon;
	}

	static String resolveSlug(Dependency dependency) {
		String id = dependency.getId();
		String candidate = SLUG_OVERRIDES.getOrDefault(id, id);
		if (AVAILABLE_SLUGS.contains(candidate)) {
			return candidate;
		}
		String groupId = dependency.getGroupId();
		if (groupId != null && groupId.startsWith(SPRING_GROUP_PREFIX)
				&& AVAILABLE_SLUGS.contains(SPRING_FALLBACK_SLUG)) {
			return SPRING_FALLBACK_SLUG;
		}
		return DEFAULT_SLUG;
	}

	private static String iconPath(String slug) {
		return ICON_PATH_PREFIX + slug + ICON_EXTENSION;
	}

	private static Set<String> scanAvailableSlugs() {
		try {
			Resource[] resources = new PathMatchingResourcePatternResolver().getResources(RESOURCE_PATTERN);
			return Arrays.stream(resources)
				.map(Resource::getFilename)
				.filter(Objects::nonNull)
				.map((name) -> name.substring(0, name.length() - ICON_EXTENSION.length()))
				.collect(Collectors.toUnmodifiableSet());
		}
		catch (IOException ex) {
			throw new IllegalStateException("Failed to scan vendor icons on the classpath", ex);
		}
	}

}
