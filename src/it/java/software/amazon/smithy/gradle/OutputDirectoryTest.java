/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.gradle;

import java.io.File;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

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
