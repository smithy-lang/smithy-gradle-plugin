package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class OutputDirectoryWithProjectionTest {
    @Test
    public void testOutputDirectory() {
        String projectName = "base-plugin/output-directory-with-projection";
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
            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertValidationRan(result);
            Utils.assertArtifactsCreated(outputDir,
                    "source/build-info/smithy-build-info.json",
                    "source/model/model.json",
                    "source/sources/main.smithy",
                    "source/sources/manifest",
                    "foo/build-info/smithy-build-info.json",
                    "foo/model/model.json",
                    "foo/sources/manifest",
                    "foo/sources/model.json");
        } catch (UncheckedIOException e) {
            throw e;
        } finally {
            Utils.deleteTempDir(buildDir);
        }
    }
}
