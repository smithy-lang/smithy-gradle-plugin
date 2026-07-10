/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import javax.inject.Inject;
import org.gradle.StartParameter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;
import software.amazon.smithy.utils.ListUtils;


/**
 * Verifies that Smithy source files are already formatted without modifying them.
 *
 * <p>This task passes {@code --check} to the Smithy CLI {@code format} command. Source
 * files are left untouched; the build fails if any file is not already formatted. This
 * is intended for CI pipelines that want to enforce consistent formatting.
 *
 * <p>Unlike {@link SmithyFormatTask}, this task is not wired into the build lifecycle
 * by default. Wire it into the {@code check} task or invoke it explicitly in CI:
 * <pre>{@code
 * ./gradlew smithyFormatCheck
 * }</pre>
 *
 * <p>Requires Smithy CLI version 1.72.0 or later.
 *
 * @see SmithyFormatTask
 */
@DisableCachingByDefault(because = "Only verifies formatting and has no cacheable outputs")
public abstract class SmithyFormatCheckTask extends SmithyFormatTask {
    private static final String DESCRIPTION = "Checks that smithy models are formatted.";

    @Inject
    public SmithyFormatCheckTask(ObjectFactory objectFactory, StartParameter startParameter) {
        super(objectFactory, startParameter);
        setDescription(DESCRIPTION);
    }

    @Override
    @TaskAction
    public void execute() {
        writeHeading("Running smithy format check");
        formatModels(ListUtils.of("--check"));
    }
}
