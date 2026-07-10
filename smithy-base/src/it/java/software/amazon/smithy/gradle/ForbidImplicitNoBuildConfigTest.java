/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ForbidImplicitNoBuildConfigTest {
    @Test
    public void testExceptionThrows() {
        Utils.withCopy("base-plugin/failure-cases/forbid-implicit-no-build-config", buildDir -> {
            BuildResult result = Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .buildAndFail();

            Assertions.assertTrue(result.getOutput()
                    .contains(
                            "No smithy-build configs found. If this was intentional, set the `smithyBuildConfigs` property to an empty list."));
            Utils.assertArtifactsNotCreated(buildDir,
                    "build/smithyprojections/forbid-implicit-no-build-config/source/build-info/smithy-build-info.json");
        });
    }
}
