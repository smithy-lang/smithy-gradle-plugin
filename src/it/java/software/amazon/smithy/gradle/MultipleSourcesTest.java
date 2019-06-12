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

public class MultipleSourcesTest {
    @Test
    public void testProjection() {
        Utils.withCopy("multiple-sources", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/multiple-sources/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/multiple-sources/source/model/model.json",
                    "build/smithyprojections/multiple-sources/source/sources/a.smithy",
                    "build/smithyprojections/multiple-sources/source/sources/b.smithy",
                    "build/smithyprojections/multiple-sources/source/sources/c.smithy",
                    "build/smithyprojections/multiple-sources/source/sources/manifest",
                    "build/libs/multiple-sources.jar");
            Utils.assertJarContains(buildDir,
                    "build/libs/multiple-sources.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/a.smithy",
                    "META-INF/smithy/b.smithy",
                    "META-INF/smithy/c.smithy");
        });
    }
}
