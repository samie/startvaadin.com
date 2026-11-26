/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui.examples;

import java.nio.file.Path;

import io.spring.initializr.generator.buildsystem.Build;
import io.spring.initializr.generator.buildsystem.MavenRepository;
import io.spring.initializr.generator.project.ProjectAssetGenerator;
import io.spring.initializr.generator.project.ProjectDescription;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.Repository;
import io.spring.initializr.web.project.ProjectGenerationInvoker;
import io.spring.initializr.web.project.ProjectRequest;
import io.spring.initializr.web.project.ProjectRequestToDescriptionConverter;

import org.springframework.context.ApplicationContext;

/**
 * Generates projects that start from a UI template ({@link Example}). On top of the
 * regular generation it adds the Vaadin Directory repository to the build, so that
 * add-ons published only there can be added to a template project without editing the
 * build's repositories first. The repository is the {@value #REPOSITORY_ID} entry of the
 * Initializr {@code env.repositories} (see {@code extra-dependencies.yml}).
 *
 * @author Sami Ekblad
 */
public class ExampleProjectGenerationInvoker extends ProjectGenerationInvoker<ProjectRequest> {

	/**
	 * Id of the Vaadin Directory repository in the Initializr metadata.
	 */
	public static final String REPOSITORY_ID = "vaadin-addons";

	public ExampleProjectGenerationInvoker(ApplicationContext applicationContext,
			ProjectRequestToDescriptionConverter<ProjectRequest> requestConverter) {
		super(applicationContext, requestConverter);
	}

	@Override
	protected ProjectAssetGenerator<Path> getProjectAssetGenerator(ProjectDescription description) {
		ProjectAssetGenerator<Path> delegate = super.getProjectAssetGenerator(description);
		return (context) -> {
			// The build is created and customized on first access, before any build file
			// is written, so the repository added here ends up in pom.xml / build.gradle.
			Repository repository = context.getBean(InitializrMetadata.class)
				.getConfiguration()
				.getEnv()
				.getRepositories()
				.get(REPOSITORY_ID);
			if (repository != null) {
				context.getBeanProvider(Build.class)
					.ifAvailable((build) -> build.repositories()
						.add(MavenRepository.withIdAndUrl(REPOSITORY_ID, repository.getUrl().toString())
							.name(repository.getName())
							.snapshotsEnabled(repository.isSnapshotsEnabled())));
			}
			return delegate.generate(context);
		};
	}

}
