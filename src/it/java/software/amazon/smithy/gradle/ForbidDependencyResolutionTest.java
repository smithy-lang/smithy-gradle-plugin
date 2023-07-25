package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ForbidDependencyResolutionTest {
    @Test
    public void testNoFork() {
        Utils.withCopy("failure-cases/forbid-dependency-resolution-no-fork", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .buildAndFail();

            Assertions.assertTrue(result.getOutput()
                    .contains("SMITHY_DEPENDENCY_MODE is set to 'forbid', but the following Maven dependencies are defined in smithy-build.json"));
            Utils.assertArtifactsNotCreated(buildDir, "build/libs/forbid-dependency-resolution.jar");
        });
    }

    @Test
    public void testFork() {
        Utils.withCopy("failure-cases/forbid-dependency-resolution-fork", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .buildAndFail();

            Assertions.assertTrue(result.getOutput()
                    .contains("SMITHY_DEPENDENCY_MODE is set to 'forbid', but the following Maven dependencies are defined in smithy-build.json"));
            Utils.assertArtifactsNotCreated(buildDir, "build/libs/forbid-dependency-resolution.jar");
        });
    }
}
