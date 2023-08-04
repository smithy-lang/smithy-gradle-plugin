package software.amazon.smithy.gradle.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gradle.api.Project;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SmithyValidateTaskTest {
    private Project testProject;

    @BeforeEach
    public void init() {
        testProject = ProjectBuilder.builder().build();
        testProject.getConfigurations().create("smithyCli");
        testProject.getConfigurations().create("smithyBuildDep");
    }

    @Test
    public void validateDefaults() {
        SmithyValidateTask validateTask = testProject.getTasks().create("smithyUnitTestValidate",
                SmithyValidateTask.class);

        assertEquals(validateTask.getShowStackTrace().get(), ShowStacktrace.INTERNAL_EXCEPTIONS);
        assertFalse(validateTask.getFork().get());
        assertFalse(validateTask.getAllowUnknownTraits().get());
        assertTrue(validateTask.getDisableModelDiscovery().get());
    }
}
