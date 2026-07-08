/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SmithyFormatTest {

    // A distinctive slice of the intentionally unformatted model. Formatting
    // collapses the extra spaces, so its presence tells us whether the file
    // was left untouched.
    private static final String UNFORMATTED_MARKER = "structure    Foo";

    @Test
    public void checkTaskFailsWithoutModifyingFiles() {
        Utils.withCopy("base-plugin/format-check", buildDir -> {
            BuildResult result = Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("smithyFormatCheck", "--stacktrace")
                    .buildAndFail();

            Assertions.assertEquals(TaskOutcome.FAILED, result.task(":smithyFormatCheck").getOutcome());

            // The CLI reports which files would be reformatted.
            Assertions.assertTrue(
                    result.getOutput().contains("would be reformatted"),
                    "Expected 'would be reformatted' in :smithyFormatCheck output but got:\n" + result.getOutput());

            // Check mode must not rewrite the source file.
            Assertions.assertTrue(
                    readModel(buildDir).contains(UNFORMATTED_MARKER),
                    "Check mode must not modify the model, but it was reformatted.");
        });
    }

    @Test
    public void formatTaskReformatsFiles() {
        Utils.withCopy("base-plugin/format-check", buildDir -> {
            BuildResult result = Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("smithyFormat", "--stacktrace")
                    .build();

            Assertions.assertEquals(TaskOutcome.SUCCESS, result.task(":smithyFormat").getOutcome());

            // Without check mode the formatter rewrites the file in place.
            Assertions.assertFalse(
                    readModel(buildDir).contains(UNFORMATTED_MARKER),
                    "Default mode must reformat the model in place, but it was left unchanged.");
        });
    }

    private static String readModel(File buildDir) throws IOException {
        File model = new File(buildDir, "model/main.smithy");
        return new String(Files.readAllBytes(model.toPath()), StandardCharsets.UTF_8);
    }
}
