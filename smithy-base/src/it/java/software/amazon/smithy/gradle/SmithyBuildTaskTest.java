/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;

public class SmithyBuildTaskTest {
    @Test
    public void testCustomBuild() {
        Utils.withCopy("base-plugin/smithy-build-task", buildDir -> {
            BuildResult result = Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("build", "--stacktrace")
                    .build();
            Utils.assertSmithyBuildDidNotRun(result);
            Utils.assertArtifactsCreated(
                    buildDir,
                    "build/smithyprojections/smithy-build-task/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/smithy-build-task/source/model/model.json",
                    "build/smithyprojections/smithy-build-task/source/sources/main.smithy",
                    "build/smithyprojections/smithy-build-task/source/sources/manifest");
        });
    }

    @Test
    public void pluginProjectionDirectoryExpressesTaskDependency() {
        Utils.withCopy("base-plugin/smithy-build-task", buildDir -> {
            BuildResult result = Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("copyOutput", "--stacktrace")
                    .build();
            Utils.assertSmithyBuildDidNotRun(result);
            Utils.assertArtifactsCreated(
                    buildDir,
                    "build/model/model.json");
        });
    }
}
