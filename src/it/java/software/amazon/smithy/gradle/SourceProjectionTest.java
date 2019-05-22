package software.amazon.smithy.gradle;/*
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

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class SourceProjectionTest {
    @Test
    public void testSourceProjection() {
        Utils.withCopy("source-projection", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .withProjectDir(buildDir)
                    .withArguments("build")
                    .build();

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/source-projection/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/source-projection/source/model/model.json",
                    "build/smithyprojections/source-projection/source/sources/main.smithy",
                    "build/smithyprojections/source-projection/source/sources/manifest",
                    "build/smithyprojections/source-projection/foo/build-info/smithy-build-info.json",
                    "build/smithyprojections/source-projection/foo/model/model.json",
                    "build/smithyprojections/source-projection/foo/sources/manifest",
                    "build/smithyprojections/source-projection/foo/sources/model.json",
                    "build/libs/source-projection.jar");
            Utils.assertJarContains(buildDir, "build/libs/source-projection.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/main.smithy");
        });
    }
}
