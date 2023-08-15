package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RequirePrerequisitePluginTest {
    @Test
    public void testNoFork() {
        Utils.withCopy("failure-cases/require-prerequisite-plugin", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .buildAndFail();

            Assertions.assertTrue(result.getOutput()
                    .contains("The smithy-jar plugin could not be applied during project evaluation."
                          + " A Java, Kotlin, or android plugin must be applied to the project first."));
            Utils.assertArtifactsNotCreated(buildDir, "build/libs/require-prerequisite-plugin.jar");
        });
    }
}
