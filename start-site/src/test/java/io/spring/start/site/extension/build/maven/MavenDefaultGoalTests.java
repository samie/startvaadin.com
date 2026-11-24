/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.extension.build.maven;

import io.spring.initializr.web.project.ProjectRequest;
import io.spring.start.site.extension.AbstractExtensionTests;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@code spring-boot:run} default goal of generated Maven projects.
 *
 * @author Sami Ekblad
 */
class MavenDefaultGoalTests extends AbstractExtensionTests {

	@Test
	void springBootRunIsTheDefaultGoal() {
		ProjectRequest request = createProjectRequest("web");
		assertThat(mavenPom(request)).containsIgnoringWhitespaces("""
				<build>
					<defaultGoal>spring-boot:run</defaultGoal>
				""");
	}

}
