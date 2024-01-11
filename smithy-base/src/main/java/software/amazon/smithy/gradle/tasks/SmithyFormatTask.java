/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.io.File;
import javax.inject.Inject;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskAction;
import software.amazon.smithy.utils.ListUtils;


/**
 * Executes the Smithy CLI {@code format} tool on a set of source files.
 *
 * <p>The smithy format tool is an opinionated formatter that can be used to maintain
 * a consistent, readable style for your smithy files. This task can be used to quickly
 * reformat smithy files as part of your gradle build process.
 *
 * <p>WARNING: This task will mutate input source files and change their formatting in-place.
 *
 * <p>Note: Smithy format was introduced to the CLI in version 1.33.0 so earlier
 * versions will be unable to use this tool.
 */
public abstract class SmithyFormatTask extends AbstractSmithyCliTask {
    private static final String DESCRIPTION = "Formats smithy models.";

    @Inject
    public SmithyFormatTask(ObjectFactory objectFactory) {
        super(objectFactory);
        setDescription(DESCRIPTION);
    }

    @TaskAction
    public void execute() {
        writeHeading("Running smithy format");
        for (final File file : getModels().get()) {
            if (file.exists() && file.isDirectory()) {
                executeCliProcess("format",
                        ListUtils.of(),
                        objectFactory.fileCollection().from(file),
                        null,
                        true
                );
            }
        }
    }
}
