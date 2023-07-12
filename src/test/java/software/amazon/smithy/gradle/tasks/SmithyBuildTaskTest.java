package software.amazon.smithy.gradle.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.gradle.internal.CliDependencyResolver;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmithyBuildTaskTest {
    private Project testProject;

    @BeforeEach
    public void init() {
        testProject = ProjectBuilder.builder().build();

        // TODO remove?
        testProject.getConfigurations().create("smithyCli");
        testProject.getConfigurations().create("smithyBuildDep");

    }

    @Test
    public void validateDefaults() {
        SmithyBuildTask buildTask = testProject.getTasks().create("smithyUnitTestBuild", SmithyBuildTask.class);

        // TODO add additional defaults
        assertEquals(buildTask.getFork().get(), false);
        assertEquals(buildTask.getShowStackTrace().get(), ShowStacktrace.INTERNAL_EXCEPTIONS);
    }
}