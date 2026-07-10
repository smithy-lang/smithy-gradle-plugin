/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;

public class OutputDirectoryTest {
    @Test
    public void testOutputDirectory() {
        String projectName = "base-plugin/output-directory";
        Path buildDirPath = Utils.createTempDir(projectName);
        File buildDir = buildDirPath.toFile();
        File outputDir = buildDirPath.resolve("build").resolve("nested-output-directory").toFile();
        try {
            Utils.copyProject(projectName, buildDir);
            BuildResult result = Utils.createGradleRunner()
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
                    "source/sources/manifest");
        } catch (UncheckedIOException e) {
            throw e;
        } finally {
            Utils.deleteTempDir(buildDir);
        }
    }
}
