package software.amazon.smithy.gradle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SmithyExtensionTest {
    private Project testProject;

    @BeforeEach
    public void init() {
        testProject = ProjectBuilder.builder().build();
    }


    @Test
    public void validateDefaults() {

        SmithyExtension extension = testProject.getExtensions().create("smithyTest", SmithyExtension.class);

        assertThat(extension.getProjectionSourceTags().get(), emptyIterable());
        assertThat(extension.getSmithyBuildConfigs().get(), contains(testProject.file("smithy-build.json")));
        assertThat(extension.getFormat().get(), equalTo(true));
        assertThat(extension.getFork().get(), equalTo(false));
        assertThat(extension.getAllowUnknownTraits().get(), equalTo(false));

        assertThat(extension.getSourceProjection().get(), equalTo("source"));
        extension.getSourceProjection().set("foo");
        assertThat(extension.getSourceProjection().get(), equalTo("foo"));
    }
}
