package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UsesExplicitlySetCLIVersionTest {
    @Test
    public void usesExplicitCliVersion() {
        Utils.withCopy("base-plugin/uses-explicitly-set-cli-version", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "-i", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertValidationRan(result);
            Assertions.assertTrue(result.getOutput().contains("(using explicitly configured Smithy CLI: 1.45.0)"));
        });
    }
}
