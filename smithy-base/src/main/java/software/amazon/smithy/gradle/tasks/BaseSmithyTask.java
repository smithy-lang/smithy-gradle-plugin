/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.util.List;
import java.util.logging.Level;
import org.gradle.StartParameter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.internal.logging.text.StyledTextOutput;
import org.gradle.internal.logging.text.StyledTextOutputFactory;
import org.gradle.work.DisableCachingByDefault;
import org.gradle.workers.WorkerExecutor;

/**
 * Base class for all Smithy tasks.
 */
@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
abstract class BaseSmithyTask extends DefaultTask {

    private final StartParameter startParameter;

    BaseSmithyTask() {
        getFork().convention(false);
        getShowStackTrace().convention(ShowStacktrace.INTERNAL_EXCEPTIONS);

        // By default, there are no build dependencies or models discovery
        getBuildClasspath().set(getProject().files());
        getModelDiscoveryClasspath().set(getProject().files());

        // if the smithyCli configuration exists use it by default
        if (getProject().getConfigurations().findByName("smithyCli") != null) {
            getCliClasspath().convention(getProject().getConfigurations()
                    .getByName("smithyCli"));
        }

        startParameter = getProject().getGradle().getStartParameter();
    }


    /**
     * Base classpath used for executing the smithy cli.
     *
     * <p>Note: this classpath must contain the smithy-cli jar.
     */
    @Classpath
    public abstract Property<FileCollection> getCliClasspath();

    /**
     * Classpath used for discovery of additional Smithy models during cli execution.
     *
     * <p>Defaults to an empty collection.
     */
    @Classpath
    @Optional
    public abstract Property<FileCollection> getModelDiscoveryClasspath();

    /**
     * Classpath to use for build dependencies.
     */
    @Classpath
    @Optional
    public abstract Property<FileCollection> getBuildClasspath();


    /**
     * Read-only property that returns the classpath used to determine the
     * classpath used when executing the cli.
     *
     * @return classpath to use when executing cli command.
     */
    @Internal
    Provider<FileCollection> getCliExecutionClasspath() {
        return getCliClasspath().zip(getBuildClasspath(), FileCollection::plus);
    }

    /**
     * Gets the list of models to execute a CLI command on.
     *
     * <p>These models are also considered "sources" when building a JAR
     * for a project. A source model is a model that appears in the
     * {@code META-INF/smithy} directory of a JAR.
     *
     * <p>This method will return an empty {@code FileCollection} and
     * never {@code null}.
     *
     * @return Returns the models to validate.
     */
    @InputFiles
    @Optional
    public abstract Property<FileCollection> getModels();

    /**
     * Whether to fork a new process for executing the smithy cli.
     *
     * <p> If false, the smithy cli will be executed in a new thread instead of a
     * new process
     *
     * <p> Defaults to {@code false}
     *
     * @return flag indicating fork setting.
     */
    @Input
    @Optional
    public abstract Property<Boolean> getFork();


    /** Sets the detail to include in stack traces.
     *
     * <p> Defaults to {@code ShowStacktrace.INTERNAL_EXCEPTIONS}
     *
     * @return stack trace setting.
     */
    @Input
    @Optional
    public abstract Property<ShowStacktrace> getShowStackTrace();


    @Internal
    WorkerExecutor getExecutor() {
        return getServices().get(WorkerExecutor.class);
    }


    /**
     * Writes header-formatted text to the build output.
     *
     * @param text text to write as a header.
     */
    protected void writeHeading(String text) {
        StyledTextOutput output = getServices().get(StyledTextOutputFactory.class)
                .create("smithy")
                .style(StyledTextOutput.Style.Header);
        output.println(text);
    }

    /**
     * Add --stacktrace and --logging settings based on Gradle's settings.
     */
     void configureLoggingOptions(final List<String> args) {
        ShowStacktrace showStacktrace = getShowStackTrace().get();
        if (showStacktrace == ShowStacktrace.ALWAYS || showStacktrace == ShowStacktrace.ALWAYS_FULL) {
            args.add("--stacktrace");
        }

        switch (startParameter.getLogLevel()) {
            case DEBUG:
                args.add("--debug");
                break;
            case LIFECYCLE: // The default setting in Gradle, so use Smithy's default of WARNING.
            case WARN:
                args.add("--logging");
                args.add(Level.WARNING.toString());
                break;
            case QUIET:
                args.add("--logging");
                args.add(Level.OFF.toString());
                break;
            case ERROR:
                args.add("--logging");
                args.add(Level.SEVERE.toString());
                break;
            case INFO:
            default:
                args.add("--logging");
                args.add(Level.INFO.toString());
                break;
        }
    }
}
