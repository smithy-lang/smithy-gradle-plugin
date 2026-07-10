/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;

public class MultipleSourcesTest {
    @Test
    public void testProjection() {
        Utils.withCopy("jar-plugin/multiple-sources", buildDir -> {
            BuildResult result = Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertValidationRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/multiple-sources/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/multiple-sources/source/model/model.json",
                    "build/smithyprojections/multiple-sources/source/sources/a.smithy",
                    "build/smithyprojections/multiple-sources/source/sources/b.smithy",
                    "build/smithyprojections/multiple-sources/source/sources/c.smithy",
                    "build/smithyprojections/multiple-sources/source/sources/nested/foo.smithy",
                    "build/smithyprojections/multiple-sources/source/sources/nested/more/baz.smithy",
                    "build/smithyprojections/multiple-sources/source/sources/manifest",
                    "build/libs/multiple-sources.jar");
            Utils.assertJarContains(buildDir,
                    "build/libs/multiple-sources.jar",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/a.smithy",
                    "META-INF/smithy/b.smithy",
                    "META-INF/smithy/c.smithy",
                    "META-INF/smithy/nested/foo.smithy",
                    "META-INF/smithy/nested/more/baz.smithy");
        });
    }
}
