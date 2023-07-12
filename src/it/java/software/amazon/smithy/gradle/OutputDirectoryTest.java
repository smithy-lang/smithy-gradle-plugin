package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class OutputDirectoryTest {
    @Test
    public void testOutputDirectory() {
        String projectName = "output-directory";
        Path buildDirPath = Utils.createTempDir(projectName);
        File buildDir = buildDirPath.toFile();
        File outputDir = buildDirPath.resolve("build").resolve("nested-output-directory").toFile();
        try {
            Utils.copyProject(projectName, buildDir);
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();
            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);
            Utils.assertArtifactsCreated(outputDir,
                    "source/build-info/smithy-build-info.json",
                    "source/model/model.json",
                    "source/sources/main.smithy",
                    "source/sources/manifest");
            Utils.assertArtifactsCreated(buildDir,
                    "build/libs/projection.jar");
            Utils.assertJarContains(buildDir,
                    "build/libs/projection.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/main.smithy");
        } catch (UncheckedIOException e) {
            throw e;
        } finally {
            Utils.deleteTempDir(buildDir);
        }
    }
}
