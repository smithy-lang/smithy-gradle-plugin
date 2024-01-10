/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.util.ArrayList;
import java.util.List;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.work.DisableCachingByDefault;
import software.amazon.smithy.gradle.SmithyUtils;

/**
 * Abstract class used to share functionality across Smithy CLI tasks
 * (that is, tasks that are meant to be run ad-hoc).
 */
@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
abstract class AbstractSmithyCliTask extends BaseSmithyTask {

    /**
     * Object factory used to create new gradle domain objects such as {@code FileCollection}s.
     */
    protected final ObjectFactory objectFactory;

    AbstractSmithyCliTask(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
        getAllowUnknownTraits().convention(false);
    }

    /** Sets whether to fail a Smithy CLI task if an unknown trait is encountered.
     *
     * <p> Defaults to {@code false}
     *
     * @return flag indicating state of allowUnknownTraits setting
     */
    @Input
    @Optional
    public abstract Property<Boolean> getAllowUnknownTraits();

    /**
     * Executes the given CLI command.
     *
     * <p>This method will take care of adding --discover, --discover-classpath,
     * and --allow-unknown-traits.
     *
     * @param command The command to execute.
     * @param additionalArgs Custom arguments that aren't one of the shared args.
     * @param sources Source files to execute the command on
     */
    final void executeCliProcess(String command,
                                 List<String> additionalArgs,
                                 FileCollection sources,
                                 FileCollection modelDiscoveryClasspath,
                                 boolean disableModelDiscovery
    ) {
        List<String> args = new ArrayList<>();
        args.add(command);

        if (modelDiscoveryClasspath != null) {
            args.add("--discover-classpath");
            args.add(modelDiscoveryClasspath.getAsPath());
        } else if (!disableModelDiscovery) {
            args.add("--discover");
        }

        configureLoggingOptions(args);
        args.addAll(additionalArgs);

        args.add("--");
        sources.forEach(file -> {
            if (file.exists()) {
                args.add(file.getAbsolutePath());
            } else {
                getLogger().error("Skipping Smithy source file because it does not exist: {}", file);
            }
        });

        SmithyUtils.executeCli(getExecutor(), args, getCliExecutionClasspath().get(), getFork().get());
    }
}
