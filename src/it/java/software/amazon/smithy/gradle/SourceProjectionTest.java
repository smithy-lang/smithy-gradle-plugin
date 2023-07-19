/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */


package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

public class SourceProjectionTest {
    @Test
    public void testSourceProjection() {
        Utils.withCopy("source-projection", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace", "--debug")
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

    @Test
    public void testSourceProjectionWithConfigurationCaching() {
        Utils.withCopy("source-projection", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace", "--debug", "--configuration-cache")
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
