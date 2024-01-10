package software.amazon.smithy.gradle;

import org.junit.jupiter.api.Test;
import software.amazon.smithy.gradle.SmithyGradleVersion;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SmithyGradleVersionTest {
    private static final String TEST_OVERRIDE_VERSION = "0.0.Alpha-Test";

    @Test
    public void versionOverrideWorks() {
        assertEquals(SmithyGradleVersion.VERSION, TEST_OVERRIDE_VERSION);
    }
}
