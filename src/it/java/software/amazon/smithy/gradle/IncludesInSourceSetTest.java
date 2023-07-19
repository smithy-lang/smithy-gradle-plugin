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

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/includes-in-sourceset/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/includes-in-sourceset/source/model/model.json",
                    "build/smithyprojections/includes-in-sourceset/source/sources/also-included.smithy",
                    "build/smithyprojections/includes-in-sourceset/source/sources/included.smithy",
                    "build/smithyprojections/includes-in-sourceset/source/sources/main.smithy",
                    "build/smithyprojections/includes-in-sourceset/source/sources/manifest",
                    "build/libs/includes-in-sourceset.jar");
            Utils.assertJarContains(buildDir,
                    "build/libs/includes-in-sourceset.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/also-included.smithy",
                    "META-INF/smithy/included.smithy",
                    "META-INF/smithy/main.smithy");
        });
    }
}
