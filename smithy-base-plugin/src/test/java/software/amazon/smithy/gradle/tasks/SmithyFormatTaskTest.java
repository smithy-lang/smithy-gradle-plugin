package software.amazon.smithy.gradle.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SmithyFormatTaskTest {
    private Project testProject;

    @BeforeEach
    public void init() {
        testProject = ProjectBuilder.builder().build();
        testProject.getConfigurations().create("smithyCli");
        testProject.getConfigurations().create("smithyBuild");
    }

    @Test
    public void validateDefaults() {


        SmithyFormatTask formatTask = testProject.getTasks().create("smithyUnitTestFormat",
                SmithyFormatTask.class);

        assertEquals(formatTask.getShowStackTrace().get(), ShowStacktrace.INTERNAL_EXCEPTIONS);
        assertFalse(formatTask.getFork().get());
        assertFalse(formatTask.getAllowUnknownTraits().get());
    }
}
