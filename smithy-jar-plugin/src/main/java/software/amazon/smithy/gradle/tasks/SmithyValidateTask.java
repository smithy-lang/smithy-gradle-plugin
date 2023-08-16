/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import javax.inject.Inject;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import software.amazon.smithy.utils.ListUtils;


/**
 * Validates the Smithy models.
 *
 * <p>The validation task will execute the Smithy CLI in a new process
 * to ensure that it uses an explicit classpath. Doing so will ensure that the
 * generated JAR works correctly when used alongside its dependencies.
 *
 */
public abstract class SmithyValidateTask extends AbstractSmithyCliTask {
    private static final String DESCRIPTION = "Validates a jar containing smithy models.";


    @Inject
    public SmithyValidateTask(ObjectFactory objectFactory) {
        super(objectFactory);
        getDisableModelDiscovery().convention(true);
        setDescription(DESCRIPTION);
    }

    /**
     * Jar file to use as a source for the Smithy CLI validate command.
     *
     * <p>This is a required input for the {@link SmithyValidateTask}. In general
     * this should be the output of a {@link org.gradle.jvm.tasks.Jar task}. For example:
     *
     * <pre>
     *     Task jarTask = project.getTasks()
     *      .getByName(JavaPlugin.JAR_TASK_NAME);
     *     ...
     *     validateTask.getJarToValidate().set(
     *      jarTask.getOutputs().getFiles());
     * </pre>
     */
    @InputFiles
    public abstract Property<FileCollection> getJarToValidate();

    /**
     * Classpath used for discovery of additional Smithy models during cli execution.
     *
     * <p>Defaults to an empty collection.
     */
    @Classpath
    @Optional
    public abstract Property<FileCollection> getModelDiscoveryClasspath();

    /**
     * Disable model discovery.
     *
     * <p>Defaults to false. This option is ignored if an explicit model discovery classpath is provided
     * in the {@code getModelDiscoveryClasspath()} property.
     *
     * @return flag indicating whether to disable model discovery
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDisableModelDiscovery();

    /**
     * Gets the classpath to use when executing the Smithy CLI.
     *
     * <p>The cli execution classpath for this task is different from other build
     * tasks because we do NOT want to include the discovery classpath for this
     * task.
     *
     * @return classpath to use for cli execution
     */
    @Internal
    @Override
    Provider<FileCollection> getCliExecutionClasspath() {
        return getResolvedCliClasspath().zip(getJarToValidate(), FileCollection::plus);
    }

    @TaskAction
    public void execute() {
        writeHeading("Running smithy validate");

        // Set models to an empty collection so source models are not included in validation path.
        executeCliProcess("validate", ListUtils.of(),
                objectFactory.fileCollection(),
                getModelDiscoveryClasspath().getOrNull(),
                getDisableModelDiscovery().get()
        );
    }
}
