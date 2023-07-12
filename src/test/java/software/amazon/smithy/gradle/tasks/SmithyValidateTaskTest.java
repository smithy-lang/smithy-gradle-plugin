package software.amazon.smithy.gradle.tasks;

import org.gradle.api.Project;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmithyValidateTaskTest {
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
        SmithyValidateTask validateTask = testProject.getTasks().create("smithyUnitTestValidate",
                SmithyValidateTask.class);

        assertEquals(validateTask.getShowStackTrace().get(), ShowStacktrace.INTERNAL_EXCEPTIONS);
        assertEquals(validateTask.getFork().get(), false);
        assertEquals(validateTask.getAllowUnknownTraits().get(), false);
        assertEquals(validateTask.getDisableModelDiscovery().get(), true);
    }
}
