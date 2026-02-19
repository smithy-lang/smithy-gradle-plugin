/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Regression test for case when smithyFormat runs on a model with a syntax error,
 * the build must fail with the real "Cannot format invalid models" message rather than a
 * secondary NoClassDefFoundError caused by smithy-model types (e.g. ShapeId) escaping the
 * isolated URLClassLoader and failing Gradle's daemon exception serializer.
 */
public class SyntaxErrorFormatTest {
    @Test
    public void formatTaskFailsWithReadableMessageOnSyntaxError() {
        Utils.withCopy("base-plugin/failure-cases/syntax-error-format", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("smithyFormat", "--stacktrace")
                    .buildAndFail();

            // The real error from the Smithy formatter must be present.
            Assertions.assertTrue(
                    result.getOutput().contains("Cannot format invalid models"),
                    "Expected 'Cannot format invalid models' in :smithyFormat output but got:\n" + result.getOutput());

            // A NoClassDefFoundError for ShapeId would indicate the classloader isolation bug has regressed.
            Assertions.assertFalse(
                    result.getOutput().contains("NoClassDefFoundError"),
                    "Unexpected NoClassDefFoundError in output -- classloader isolation regression:\n"
                            + result.getOutput());

            // The smithyFormat task itself must be the one that failed, not some other task.
            Assertions.assertEquals(TaskOutcome.FAILED, result.task(":smithyFormat").getOutcome());
        });
    }
}
