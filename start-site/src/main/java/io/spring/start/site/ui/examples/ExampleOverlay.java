/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

/**
 * Overlays an {@link Example}'s resource tree onto a generated project. Files live under
 * {@code classpath:/examples/<id>/} mirroring the target layout; the {@code __pkg__} path
 * segment is replaced with the project package and the {@code {{package}}} token is
 * substituted inside text files.
 *
 * @author Sami Ekblad
 */
public final class ExampleOverlay {

	private static final String PACKAGE_SEGMENT = "__pkg__";

	private static final String PACKAGE_TOKEN = "{{package}}";

	private static final String TEMPLATE_SUFFIX = ".tmpl";

	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	public void apply(Path projectRoot, Example example, String packageName) {
		try {
			String base = example.resourceBase();
			String rootUri = this.resolver.getResource("classpath:/" + base).getURI().toString();
			for (Resource resource : this.resolver.getResources("classpath*:/" + base + "**")) {
				String uri = resource.getURI().toString();
				if (!uri.startsWith(rootUri)) {
					continue;
				}
				String relative = stripLeadingSlash(uri.substring(rootUri.length()));
				if (relative.isEmpty() || isDirectory(resource, relative)) {
					continue;
				}
				writeFile(projectRoot, resource, relative, packageName);
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private void writeFile(Path projectRoot, Resource resource, String relative, String packageName)
			throws IOException {
		String target = relative.replace(PACKAGE_SEGMENT, packageName.replace('.', '/'));
		if (target.endsWith(TEMPLATE_SUFFIX)) {
			target = target.substring(0, target.length() - TEMPLATE_SUFFIX.length());
		}
		Path path = projectRoot.resolve(target);
		Files.createDirectories(path.getParent());
		try (InputStream input = resource.getInputStream()) {
			String content = StreamUtils.copyToString(input, StandardCharsets.UTF_8)
				.replace(PACKAGE_TOKEN, packageName);
			Files.writeString(path, content);
		}
	}

	private static boolean isDirectory(Resource resource, String relative) {
		if (relative.endsWith("/")) {
			return true;
		}
		try {
			return resource.getFile().isDirectory();
		}
		catch (IOException ex) {
			return false;
		}
	}

	private static String stripLeadingSlash(String value) {
		return value.startsWith("/") ? value.substring(1) : value;
	}

}
