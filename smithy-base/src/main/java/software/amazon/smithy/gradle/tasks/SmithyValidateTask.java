/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import software.amazon.smithy.model.validation.Severity;


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
        getAllowUnknownTraits().convention(false);
        getDisableModelDiscovery().convention(false);
        getSeverity().convention(Severity.ERROR.toString());
        setDescription(DESCRIPTION);
    }

    /**
     * Files to use as a sources for the Smithy CLI {@code validate} command.
     *
     * <p>This is a required input of the SmithyValidate task.
     *
     * @return file collection to use as sources for the validate task.
     */
    @InputFiles
    public abstract Property<FileCollection> getSources();

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
     * Set the minimum reported validation severity.
     *
     * <p>This value should be one of: NOTE, WARNING, DANGER, ERROR [default].
     *
     * @return minimum validator severity
     */
    @Input
    @Optional
    public abstract Property<String> getSeverity();

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
        return getCliClasspath();
    }

    @TaskAction
    public void execute() {
        writeHeading("Running smithy validate");

        // Add validator severity settings
        List<String> extraArgs = new ArrayList<>();
        extraArgs.add("--severity");
        extraArgs.add(getSeverity().get());

        // Set models to an empty collection so source models are not included in validation path.
        executeCliProcess("validate", extraArgs,
                getSources().get(),
                getDisableModelDiscovery().get()
        );
    }
}
