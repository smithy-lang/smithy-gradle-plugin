package software.amazon.smithy.gradle.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmithyJarStagingTaskTest {
    private Project testProject;

    @BeforeEach
    public void init() {
        testProject = ProjectBuilder.builder().build();
    }

    @Test
    public void validateDefaults() {
        SmithyJarStagingTask stagingTask = testProject.getTasks().create("smithyUnitTestJarStaging",
                SmithyJarStagingTask.class);

        assertEquals(stagingTask.getProjection().get(), "source");
        assertEquals(stagingTask.getBaseBuildDir().get(), testProject.getLayout().getBuildDirectory().get());
    }
}
