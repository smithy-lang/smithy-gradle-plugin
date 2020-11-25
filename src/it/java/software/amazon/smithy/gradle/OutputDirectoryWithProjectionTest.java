/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class OutputDirectoryWithProjectionTest {
    @Test
    public void testOutputDirectory() {
        File buildDir = Paths.get("/tmp/build-directory").toFile();
        File outputDir = Paths.get("/tmp/output-directory").toFile();
        try {
            Utils.copyProject("output-directory-with-projection", buildDir);
            BuildResult result = GradleRunner.create()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();
            Utils.assertSmithyBuildRan(result);
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
            Utils.assertArtifactsCreated(buildDir,
                    "build/libs/projection.jar");
            Utils.assertJarContains(buildDir,
                    "build/libs/projection.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/model.json");
        } catch (UncheckedIOException e) {
            throw e;
        } finally {
                Utils.deleteTempDir(buildDir);
                Utils.deleteTempDir(outputDir);
        }
    }
}
