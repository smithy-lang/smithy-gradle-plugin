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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class ProjectionTest {
    @Test
    public void testProjection() {
        Utils.withCopy("projection", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/projection/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/projection/source/model/model.json",
                    "build/smithyprojections/projection/source/sources/main.smithy",
                    "build/smithyprojections/projection/source/sources/manifest",
                    "build/smithyprojections/projection/foo/build-info/smithy-build-info.json",
                    "build/smithyprojections/projection/foo/model/model.json",
                    "build/smithyprojections/projection/foo/sources/manifest",
                    "build/smithyprojections/projection/foo/sources/model.json",
                    "build/libs/projection.jar");
            Utils.assertJarContains(buildDir,
                    "build/libs/projection.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/model.json");
        });
    }

    @Test
    public void usesWarningLoggingByDefault() {
        Utils.withCopy("projection", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build")
                    .build();

            // This string appears only when logging INFO or higher.
            assertThat(result.getOutput(), not(containsString("[INFO] Validating")));
        });
    }

    @Test
    public void modifiesLogging() {
        Utils.withCopy("projection", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--info")
                    .build();
            assertThat(result.getOutput(), containsString("[INFO] Smithy validation complete"));
        });
    }

    @Test
    public void usesDebugMode() {
        Utils.withCopy("projection", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--debug")
                    .build();

            // This string only appears with debug logging.
            assertThat(result.getOutput(), containsString("Found Smithy model"));
        });
    }
}
