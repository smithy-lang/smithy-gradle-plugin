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

public class DisableJarTest {
    @Test
    public void testProjection() {
        Utils.withCopy("disable-jar", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationDidNotRun(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/disable-jar/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/disable-jar/source/model/model.json",
                    "build/smithyprojections/disable-jar/source/sources/main.smithy",
                    "build/smithyprojections/disable-jar/source/sources/manifest");
            Utils.assertArtifactsNotCreated(buildDir, "build/libs/disable-jar.jar");
        });
    }
}
