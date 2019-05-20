package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MissingRuntimeDependencyTest {
    @Test
    public void testProjection() {
        Utils.withCopy("failure-cases/missing-runtime-dependency", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .withProjectDir(buildDir)
                    .withArguments("build")
                    .buildAndFail();

            Assertions.assertTrue(result.getOutput().contains("Unable to resolve trait"));
        });
    }
}
