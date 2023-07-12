/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import java.io.File;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class SmithyBuildTaskTest {
    @Test
    public void testCustomBuild() {
        // TODO: Fix
        //Utils.withCopy("smithy-build-task", this::run);
    }

    private void run(File buildDir) {
        BuildResult result = GradleRunner.create()
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
        Utils.assertArtifactsNotCreated(buildDir, "build/libs/smithy-build-task.jar");
    }
}
