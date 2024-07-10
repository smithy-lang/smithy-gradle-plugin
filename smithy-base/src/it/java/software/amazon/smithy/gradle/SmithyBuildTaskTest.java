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

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;

public class SmithyBuildTaskTest {
    @Test
    public void testCustomBuild() {
        Utils.withCopy("base-plugin/smithy-build-task", buildDir -> {
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
        });
    }

    @Test
    public void pluginProjectionDirectoryExpressesTaskDependency() {
        Utils.withCopy("base-plugin/smithy-build-task", buildDir -> {
            BuildResult result = GradleRunner.create()
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
