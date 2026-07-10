/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle;

import java.io.File;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;

public class OutputDirectoryConfigTest {
    @Test
    public void testOutputDirectoryBuild() {
        Utils.withCopy("base-plugin/output-directory-config", buildDir -> {
            BuildResult result = Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();
            File outputDir = buildDir.toPath()
                    .resolve("build")
                    .resolve("nested-output-directory")
                    .toFile();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertValidationRan(result);

            Utils.assertArtifactsCreated(outputDir,
                    "source/build-info/smithy-build-info.json",
                    "source/model/model.json",
                    "source/sources/main.smithy",
                    "source/sources/manifest");
        });
    }
}
