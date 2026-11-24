/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui.examples;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.spring.start.site.ui.InitializrFormModel;
import io.spring.start.site.ui.InitializrUiService;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Generates a project for each example through the same path as the UI's Generate button,
 * then builds it with {@code mvnw verify}, which runs the example's own tests. Skipped
 * unless {@code -Dexamples=<id>[,<id>...]} or {@code -Dexamples=all} is set. The
 * generated projects are left in {@code target/example-projects/<id>} for further checks.
 *
 * @author Sami Ekblad
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "examples", matches = ".+")
class ExampleProjectsVerificationTests {

	private static final Duration BUILD_TIMEOUT = Duration.ofMinutes(20);

	private static final Path OUTPUT = Path.of("target", "example-projects");

	@Autowired
	private InitializrUiService service;

	static Stream<String> examples() {
		List<String> requested = Arrays.asList(System.getProperty("examples", "").split(","));
		return ExampleCatalog.all()
			.stream()
			.map(Example::id)
			.filter((id) -> requested.contains("all") || requested.contains(id));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("examples")
	void exampleProjectBuilds(String id) throws IOException, InterruptedException {
		InitializrFormModel model = this.service.defaultFormModel();
		model.getDependencies().addAll(ExampleCatalog.find(id).dependencies());
		model.setExampleId(id);
		Path root = OUTPUT.resolve(id);
		deleteRecursively(root);
		unzip(this.service.generateZip(model), root);
		Path project = root.resolve(model.getArtifactId());
		Path log = OUTPUT.resolve(id + ".log");
		int exitCode = mavenVerify(project, log);
		assertThat(exitCode)
			.describedAs("Build of example '%s' failed, see %s%n%s", id, log.toAbsolutePath(), tail(log))
			.isZero();
	}

	private int mavenVerify(Path project, Path log) throws IOException, InterruptedException {
		Path mvnw = project.resolve("mvnw");
		mvnw.toFile().setExecutable(true);
		ProcessBuilder processBuilder = new ProcessBuilder(mvnw.toAbsolutePath().toString(), "-B", "-ntp", "verify");
		processBuilder.environment().remove("JAVA_TOOL_OPTIONS");
		processBuilder.directory(project.toFile());
		processBuilder.redirectErrorStream(true);
		processBuilder.redirectOutput(log.toFile());
		Process process = processBuilder.start();
		if (!process.waitFor(BUILD_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)) {
			process.descendants().forEach(ProcessHandle::destroyForcibly);
			process.destroyForcibly();
			return -1;
		}
		return process.exitValue();
	}

	private static void unzip(byte[] zip, Path target) throws IOException {
		try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {
				Path file = target.resolve(entry.getName()).normalize();
				if (entry.isDirectory()) {
					Files.createDirectories(file);
				}
				else {
					Files.createDirectories(file.getParent());
					Files.copy(in, file);
				}
			}
		}
	}

	private static void deleteRecursively(Path root) throws IOException {
		if (Files.exists(root)) {
			try (Stream<Path> walk = Files.walk(root)) {
				walk.sorted(Comparator.reverseOrder()).forEach((path) -> path.toFile().delete());
			}
		}
	}

	private static String tail(Path log) throws IOException {
		List<String> lines = Files.readAllLines(log);
		return String.join("\n", lines.subList(Math.max(0, lines.size() - 60), lines.size()));
	}

}
