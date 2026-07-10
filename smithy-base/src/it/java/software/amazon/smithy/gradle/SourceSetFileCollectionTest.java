/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;

public class SourceSetFileCollectionTest {
    // Verifies the "smithy" source set can be consumed directly wherever Gradle accepts a
    // FileCollection. The source set is our own SourceDirectorySet implementation, so this guards
    // against it failing to interoperate with Gradle's file APIs (e.g. an internal cast) when
    // passed to a Copy task's from(...).
    @Test
    public void consumesSourceSetAsFileCollection() {
        Utils.withCopy("base-plugin/source-set-file-collection", buildDir -> {
            BuildResult result = Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/collected-smithy-sources/main.smithy");
        });
    }
}
