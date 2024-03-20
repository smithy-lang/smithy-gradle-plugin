/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.gradle.StartParameter;
import org.gradle.api.GradleException;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.work.DisableCachingByDefault;


/**
 * Executes the Smithy CLI {@code select} command on a set of source files.
 *
 * <p>This task queries a set of models from the provided sources using a selector.
 *
 * <p>NOTE: this task must be executed with the command line option `--selector` set.
 *
 * @see <a href="https://smithy.io/2.0/spec/selectors.html#selectors">Smithy Selectors</a>
 */
@DisableCachingByDefault(because = "Select task should only be called manually.")
public abstract class SmithySelectTask extends AbstractSmithyCliTask {
    private static final String DESCRIPTION = "Queries Smithy models with a selector.";

    @Inject
    public SmithySelectTask(ObjectFactory objectFactory, StartParameter startParameter) {
        super(objectFactory, startParameter);
        setDescription(DESCRIPTION);
    }

    @Input
    @Option(option = "selector", description = "The Smithy selector to execute")
    abstract Property<String> getSelector();


    @Input
    @Optional
    @Option(option = "show", description = "The Smithy selector to execute")
    abstract Property<String> getShow();

    @Input
    @Optional
    @Option(option = "show-traits", description = "The Smithy selector to execute")
    abstract Property<String> getShowTraits();

    @TaskAction
    public void execute() {
        if (!getSelector().isPresent()) {
            throw new GradleException("Select task requires that the command line option `--select` be set.");
        }
        List<String> extraArgs = new ArrayList<>();
        extraArgs.add("--selector");
        extraArgs.add(getSelector().get());

        if (getShow().isPresent()) {
            extraArgs.add("--show");
            extraArgs.add(getShow().get());
        }

        if (getShowTraits().isPresent()) {
            extraArgs.add("--show-traits");
            extraArgs.add(getShowTraits().get());
        }

        executeCliProcess("select",
                extraArgs,
                getModels().get(),
                true
        );
    }
}
