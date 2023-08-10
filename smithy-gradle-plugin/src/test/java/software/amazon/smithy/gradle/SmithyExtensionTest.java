package software.amazon.smithy.gradle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

public class SmithyExtensionTest {
    @Test
    public void projectionDefaultsToSource() {
        SmithyExtension extension = new SmithyExtension();

        assertThat(extension.getProjection(), equalTo("source"));
        extension.setProjection("foo");
        assertThat(extension.getProjection(), equalTo("foo"));
    }
}
