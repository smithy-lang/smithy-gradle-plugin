package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class ScansForCliVersionTest {
    @Test
    public void scansForCliVersion() {
        Utils.withCopy("scans-for-cli-version", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);
            Utils.assertJarContains(buildDir,
                    "build/libs/scans-for-cli-version-9.9.9.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/main.smithy");
        });
    }
}
