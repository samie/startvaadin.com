/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Overlays an {@link Example}'s resource tree onto a generated project. Files live under
 * {@code classpath:/examples/<id>/} mirroring the target layout; the {@code __pkg__} path
 * segment is replaced with the project package. Inside text files, {@code {{package}}} is
 * replaced with the package, and {@code {{name}}} and {@code {{description}}} with the
 * project's name and description, escaped for use inside a Java string literal.
 *
 * @author Sami Ekblad
 */
public final class ExampleOverlay {

	private static final String PACKAGE_SEGMENT = "__pkg__";

	private static final String PACKAGE_TOKEN = "{{package}}";

	private static final String NAME_TOKEN = "{{name}}";

	private static final String DESCRIPTION_TOKEN = "{{description}}";

	private static final String TEMPLATE_SUFFIX = ".tmpl";

	private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

	public void apply(Path projectRoot, Example example, Project project) {
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
				writeFile(projectRoot, resource, relative, project);
			}
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private void writeFile(Path projectRoot, Resource resource, String relative, Project project) throws IOException {
		String target = relative.replace(PACKAGE_SEGMENT, project.packageName().replace('.', '/'));
		if (target.endsWith(TEMPLATE_SUFFIX)) {
			target = target.substring(0, target.length() - TEMPLATE_SUFFIX.length());
		}
		Path path = projectRoot.resolve(target);
		Files.createDirectories(path.getParent());
		try (InputStream input = resource.getInputStream()) {
			byte[] bytes = input.readAllBytes();
			String text = decodeUtf8(bytes);
			if (text != null) {
				Files.writeString(path,
						text.replace(PACKAGE_TOKEN, project.packageName())
							.replace(NAME_TOKEN, javaString(project.name()))
							.replace(DESCRIPTION_TOKEN, javaString(project.description())));
			}
			else {
				Files.write(path, bytes);
			}
		}
	}

	private static String javaString(String value) {
		return (value != null) ? value.replace("\\", "\\\\").replace("\"", "\\\"").replaceAll("\\s+", " ").strip() : "";
	}

	/**
	 * Decode a file as UTF-8 text, so that the tokens can be substituted.
	 * @param bytes the file content
	 * @return the text, or {@code null} for a binary file, such as an image, which is
	 * copied unchanged
	 */
	private static String decodeUtf8(byte[] bytes) {
		try {
			return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
		}
		catch (CharacterCodingException ex) {
			return null;
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

	/**
	 * The project details an example can refer to.
	 *
	 * @param packageName the root package
	 * @param name the project name
	 * @param description the project description
	 */
	public record Project(String packageName, String name, String description) {

	}

}
