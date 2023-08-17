package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class SyntaxErrorTest {
    @Test
    public void testFailsWithSyntaxError() {
        Utils.withCopy("failure-cases/syntax-error", buildDir -> {
            GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .buildAndFail();

            Utils.assertArtifactsNotCreated(buildDir,
                    "build/smithyprojections/syntax-error/source/build-info/smithy-build-info.json");
        });
    }
}
