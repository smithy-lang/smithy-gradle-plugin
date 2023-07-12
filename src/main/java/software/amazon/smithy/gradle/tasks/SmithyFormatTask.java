/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.io.File;
import org.gradle.api.tasks.TaskAction;
import software.amazon.smithy.utils.ListUtils;


public abstract class SmithyFormatTask extends AbstractSmithyCliTask {
    @TaskAction
    public void execute() {
        writeHeading("Running smithy format");
        for (final File file : getModels().get()) {
            if (file.exists() && file.isDirectory()) {
                executeCliProcess("format", ListUtils.of(), getProject().files(file));
            }
        }
    }
}
