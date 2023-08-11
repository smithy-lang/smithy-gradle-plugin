package software.amazon.smithy.gradle;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SmithyBaseExtensionTest {
    private Project testProject;

    @BeforeEach
    public void init() {
        testProject = ProjectBuilder.builder().build();
    }


    @Test
    public void validateDefaults() {

        SmithyBaseExtension extension = testProject.getExtensions().create("smithyTest", SmithyBaseExtension.class);

        assertThat(extension.getProjectionSourceTags().get(), emptyIterable());
        assertThat(extension.getSmithyBuildConfigs().get(), contains(testProject.file("smithy-build.json")));
        assertTrue(extension.getFormat().get());
        assertFalse(extension.getFork().get());
        assertFalse(extension.getAllowUnknownTraits().get());

        assertThat(extension.getSourceProjection().get(), equalTo("source"));
        extension.getSourceProjection().set("foo");
        assertThat(extension.getSourceProjection().get(), equalTo("foo"));
    }
}
