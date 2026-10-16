/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.aura.Aura;

/**
 * Configures the Vaadin application shell and loads the Aura theme.
 * <p>
 * The {@code assets} mapping copies the curated set of simple-icons SVGs into the static
 * resource folder, served at {@code /assets/vendor-icons/icons/<slug>.svg}. The slug list
 * here must stay in sync with {@code VendorIcons.AVAILABLE_SLUGS}.
 *
 * @author Vaadin UI Migration
 */
@StyleSheet(Aura.STYLESHEET)
@StyleSheet("context://styles/app.css")
@NpmPackage(value = "simple-icons", version = "16.35.0", assets = { "icons/vaadin.svg:vendor-icons",
		"icons/postgresql.svg:vendor-icons", "icons/mysql.svg:vendor-icons", "icons/mariadb.svg:vendor-icons",
		"icons/mongodb.svg:vendor-icons", "icons/redis.svg:vendor-icons", "icons/neo4j.svg:vendor-icons",
		"icons/apachecassandra.svg:vendor-icons", "icons/apachekafka.svg:vendor-icons",
		"icons/elasticsearch.svg:vendor-icons", "icons/rabbitmq.svg:vendor-icons", "icons/openjdk.svg:vendor-icons",
		"icons/docker.svg:vendor-icons", "icons/graphql.svg:vendor-icons", "icons/springsecurity.svg:vendor-icons",
		"icons/spring.svg:vendor-icons", "icons/okta.svg:vendor-icons", "icons/thymeleaf.svg:vendor-icons",
		"icons/apachefreemarker.svg:vendor-icons", "icons/apachegroovy.svg:vendor-icons",
		"icons/kotlin.svg:vendor-icons", "icons/sqlite.svg:vendor-icons", "icons/couchbase.svg:vendor-icons",
		"icons/apachepulsar.svg:vendor-icons", "icons/consul.svg:vendor-icons", "icons/vault.svg:vendor-icons",
		"icons/netflix.svg:vendor-icons", "icons/googlecloud.svg:vendor-icons", "icons/googlepubsub.svg:vendor-icons",
		"icons/googlecloudstorage.svg:vendor-icons", "icons/opensearch.svg:vendor-icons",
		"icons/swagger.svg:vendor-icons", "icons/sentry.svg:vendor-icons", "icons/datadog.svg:vendor-icons",
		"icons/dynatrace.svg:vendor-icons", "icons/influxdb.svg:vendor-icons", "icons/newrelic.svg:vendor-icons",
		"icons/prometheus.svg:vendor-icons", "icons/opentelemetry.svg:vendor-icons", "icons/anthropic.svg:vendor-icons",
		"icons/mistralai.svg:vendor-icons", "icons/ollama.svg:vendor-icons", "icons/googlegemini.svg:vendor-icons",
		"icons/elevenlabs.svg:vendor-icons", "icons/milvus.svg:vendor-icons", "icons/github.svg:vendor-icons",
		"icons/h2database.svg:vendor-icons", "icons/cloudfoundry.svg:vendor-icons", "icons/vmware.svg:vendor-icons",
		"icons/modelcontextprotocol.svg:vendor-icons", "icons/markdown.svg:vendor-icons",
		"icons/apache.svg:vendor-icons", "icons/htmx.svg:vendor-icons", "icons/deepseek.svg:vendor-icons",
		"icons/qdrant.svg:vendor-icons" })
public class AppShell implements AppShellConfigurator {

}
