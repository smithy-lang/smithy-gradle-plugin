package software.amazon.smithy.gradle.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.gradle.internal.CliDependencyResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SmithyBuildTaskTest {
    private Project testProject;

    @BeforeEach
    public void init() {
        testProject = ProjectBuilder.builder().build();
        testProject.getConfigurations().create("smithyCli");
        testProject.getConfigurations().create("smithyBuildDep");
    }

    @Test
    public void validateDefaults() {
        SmithyBuildTask buildTask = testProject.getTasks().create("smithyUnitTestBuild", SmithyBuildTask.class);

        assertFalse(buildTask.getFork().get());
        assertEquals(buildTask.getShowStackTrace().get(), ShowStacktrace.INTERNAL_EXCEPTIONS);
        assertEquals(buildTask.getSourceProjection().get(), "source");
    }
}