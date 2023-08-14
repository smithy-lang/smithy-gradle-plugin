package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class IncludesInSourceSetTest {
    @Test
    public void testSourceProjection() {
        Utils.withCopy("includes-in-sourceset", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/includes-in-sourceset/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/includes-in-sourceset/source/model/model.json",
                    "build/smithyprojections/includes-in-sourceset/source/sources/also-included.smithy",
                    "build/smithyprojections/includes-in-sourceset/source/sources/included.smithy",
                    "build/smithyprojections/includes-in-sourceset/source/sources/main.smithy",
                    "build/smithyprojections/includes-in-sourceset/source/sources/manifest");
        });
    }
}
