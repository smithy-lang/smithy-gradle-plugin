package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;

public class OutputDirectoryConfigTest {
    @Test
    public void testOutputDirectoryBuild() {
        Utils.withCopy("output-directory-config", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();
            File outputDir = buildDir.toPath().resolve("build")
                    .resolve("nested-output-directory").toFile();

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);

            Utils.assertArtifactsCreated(outputDir,
                    "source/build-info/smithy-build-info.json",
                    "source/model/model.json",
                    "source/sources/main.smithy",
                    "source/sources/manifest");
            Utils.assertArtifactsCreated(buildDir,
                    "build/libs/output-directory-config.jar");
            Utils.assertJarContains(buildDir,
                    "build/libs/output-directory-config.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/main.smithy");
        });
    }
}
