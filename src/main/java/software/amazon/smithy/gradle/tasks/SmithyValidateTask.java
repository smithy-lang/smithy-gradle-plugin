/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import javax.inject.Inject;
import org.gradle.api.file.FileCollection;
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
 * to ensure that it uses an explicit classpath that ensures that the
 * generated JAR works correctly when used alongside its dependencies.
 *
 * <p>The CLI version used to validate the generated JAR is picked by
 * searching for smithy-model in the runtime dependencies. If found,
 * the same version of the CLI is used. If not found, a default version
 * is used.
 */
public abstract class SmithyValidateTask extends AbstractSmithyCliTask {

    @Inject
    public SmithyValidateTask() {
        getDisableModelDiscovery().convention(true);
    }

    @InputFiles
    public abstract Property<FileCollection> getJarToValidate();

    @Classpath
    @Optional
    public abstract Property<FileCollection> getModelDiscoveryClasspath();

    /** Disable model discovery.
     *
     * <p> Defaults to false. This option is ignored if an explicit model discovery classpath is provided
     * in the {@code getModelDiscoveryClasspath()} property.
     *
     * @return flag indicating whether to disable model discovery
     */
    @Input
    @Optional
    public abstract Property<Boolean> getDisableModelDiscovery();

    /** The cli execution classpath for this task is different than other build
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
                getProject().files(),
                getModelDiscoveryClasspath().getOrNull(),
                getDisableModelDiscovery().get()
        );
    }
}
