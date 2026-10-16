/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import java.util.Map;
import java.util.Set;

import com.vaadin.flow.component.icon.SvgIcon;
import io.spring.initializr.metadata.Dependency;

/**
 * Resolves a dependency to a vendor {@link SvgIcon}. Icons are curated SVGs from the
 * simple-icons npm package, copied into the static resource folder at build time by the
 * {@code @NpmPackage(assets = ...)} mapping on {@link AppShell} and served under
 * {@code /assets/vendor-icons}. Dependencies without a matching vendor icon fall back to
 * a generic Spring icon when their resolved Maven group id is Spring-authored
 * ({@code org.springframework*}), or to a generic OpenJDK icon otherwise.
 *
 * @author Sami Ekblad
 */
public final class VendorIcons {

	private static final String ICON_PATH_PREFIX = "/assets/vendor-icons/icons/";

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
			Map.entry("spring-ai-vertexai-embeddings", "googlecloud"), Map.entry("h2", "h2database"),
			Map.entry("tanzu-governance-starter", "vmware"), Map.entry("tanzu-scg-access-control", "vmware"),
			Map.entry("tanzu-scg-custom", "vmware"), Map.entry("tanzu-scg-graphql", "vmware"),
			Map.entry("tanzu-scg-sso", "vmware"), Map.entry("tanzu-scg-traffic-control", "vmware"),
			Map.entry("tanzu-scg-transformation", "vmware"), Map.entry("tanzu-spring-sdk", "vmware"),
			Map.entry("spring-ai-deepseek", "deepseek"), Map.entry("spring-ai-mcp-server", "modelcontextprotocol"),
			Map.entry("spring-ai-mcp-client", "modelcontextprotocol"),
			Map.entry("mcp-security", "modelcontextprotocol"), Map.entry("spring-ai-vectordb-qdrant", "qdrant"),
			Map.entry("spring-ai-markdown-document-reader", "markdown"),
			Map.entry("spring-ai-tika-document-reader", "apache"),
			Map.entry("spring-ai-chat-memory-repository-redis", "redis"),
			Map.entry("spring-ai-chat-memory-repository-cassandra", "apachecassandra"),
			Map.entry("spring-ai-chat-memory-repository-mongodb", "mongodb"),
			Map.entry("spring-ai-chat-memory-repository-neo4j", "neo4j"));

	/**
	 * Slugs copied into the static resource folder. Must stay in sync with the
	 * {@code assets} glob on {@link AppShell}.
	 */
	private static final Set<String> AVAILABLE_SLUGS = Set.of("vaadin", "postgresql", "mysql", "mariadb", "mongodb",
			"redis", "neo4j", "apachecassandra", "apachekafka", "elasticsearch", "rabbitmq", "openjdk", "docker",
			"graphql", "springsecurity", "spring", "okta", "thymeleaf", "apachefreemarker", "apachegroovy", "kotlin",
			"sqlite", "couchbase", "apachepulsar", "consul", "vault", "netflix", "googlecloud", "googlepubsub",
			"googlecloudstorage", "opensearch", "swagger", "sentry", "datadog", "dynatrace", "influxdb", "newrelic",
			"prometheus", "opentelemetry", "anthropic", "mistralai", "ollama", "googlegemini", "elevenlabs", "milvus",
			"github", "h2database", "cloudfoundry", "vmware", "modelcontextprotocol", "markdown", "apache", "htmx",
			"deepseek", "qdrant");

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

}
