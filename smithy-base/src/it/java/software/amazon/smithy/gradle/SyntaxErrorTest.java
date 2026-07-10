/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle;

import org.junit.jupiter.api.Test;

public class SyntaxErrorTest {
    @Test
    public void testFailsWithSyntaxError() {
        Utils.withCopy("base-plugin/failure-cases/syntax-error", buildDir -> {
            Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .buildAndFail();

            Utils.assertArtifactsNotCreated(buildDir,
                    "build/smithyprojections/syntax-error/source/build-info/smithy-build-info.json");
        });
    }
}
