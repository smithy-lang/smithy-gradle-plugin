package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConflictingOutputDirectoriesTest {
    @Test
    public void testConflictingConfigs() {
        Utils.withCopy("failure-cases/conflicting-output-dir-configs", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .buildAndFail();

            Assertions.assertTrue(result.getOutput()
                    .contains("Conflicting output directories defined in provided smithy build configs"));
            Utils.assertArtifactsNotCreated(buildDir, "build/libs/conflicting-output-dirs.jar");
        });
    }
}
