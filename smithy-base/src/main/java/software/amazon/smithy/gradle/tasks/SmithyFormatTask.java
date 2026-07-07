/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.io.File;
import java.util.List;
import javax.inject.Inject;
import org.gradle.StartParameter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import software.amazon.smithy.utils.ListUtils;


/**
 * Executes the Smithy CLI {@code format} tool on a set of source files.
 *
 * <p>The smithy format tool is an opinionated formatter that can be used to maintain
 * a consistent, readable style for your smithy files. This task can be used to quickly
 * reformat smithy files as part of your gradle build process.
 *
 * <p>WARNING: This task will mutate input source files and change their formatting in-place.
 * When check mode is enabled (via the {@code --check} command-line flag) it instead leaves
 * the files untouched and fails the build if any of them are not already formatted.
 *
 * <p>Note: Smithy format was introduced to the CLI in version 1.33.0 so earlier
 * versions will be unable to use this tool. The {@code check} mode requires CLI version
 * 1.72.0 or later.
 */
public abstract class SmithyFormatTask extends AbstractSmithyCliTask {
    private static final String DESCRIPTION = "Formats smithy models.";

    private boolean check;

    @Inject
    public SmithyFormatTask(ObjectFactory objectFactory, StartParameter startParameter) {
        super(objectFactory, startParameter);
        setDescription(DESCRIPTION);
    }

    /**
     * Whether to check formatting instead of applying it.
     *
     * <p>When {@code true}, the task passes {@code --check} to the Smithy CLI. Source
     * files will be left unmodified and the build will fail if any file is not already
     * formatted. This is useful in CI to assert that committed models are formatted.
     *
     * <p>This property is only intended to be set via the {@code --check} command-line
     * flag when explicitly invoking the task, not from build scripts.
     *
     * <p>Defaults to {@code false}.
     *
     * @return whether to check formatting rather than apply it.
     */
    @Internal
    public boolean getCheck() {
        return check;
    }

    // By not exposing check as a Property, we ensure that it doesn't get generated
    // in the DSL as a settable property. It's not impossible to call setCheck, but
    // hopefully the lack of the obvious method of setting this property discourages
    // that pattern of use.
    @Option(option = "check",
            description = "Fail if any file is not formatted instead of reformatting it in-place.")
    public void setCheck(boolean check) {
        this.check = check;
    }

    @TaskAction
    public void execute() {
        writeHeading("Running smithy format");
        List<String> additionalArgs = check ? ListUtils.of("--check") : ListUtils.of();
        for (final File file : getModels().get()) {
            if (file.exists() && file.isDirectory()) {
                executeCliProcess("format",
                        additionalArgs,
                        objectFactory.fileCollection().from(file),
                        true
                );
            }
        }
    }
}
