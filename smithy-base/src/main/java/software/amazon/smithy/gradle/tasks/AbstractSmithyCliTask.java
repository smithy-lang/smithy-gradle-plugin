/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.gradle.StartParameter;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.api.model.ObjectFactory;
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
import software.amazon.smithy.gradle.SmithyUtils;

/**
 * Abstract class used to share functionality across Smithy CLI tasks
 * (that is, tasks that are meant to be run ad-hoc).
 */
@DisableCachingByDefault(because = "Abstract super-class, not to be instantiated directly")
abstract class AbstractSmithyCliTask extends DefaultTask {

    /**
     * Object factory used to create new gradle domain objects such as {@code FileCollection}s.
     */
    protected final ObjectFactory objectFactory;
    private final StartParameter startParameter;

    AbstractSmithyCliTask(ObjectFactory objectFactory, StartParameter startParameter) {
        this.objectFactory = objectFactory;
        this.startParameter = startParameter;

        getFork().convention(false);
        getShowStackTrace().convention(ShowStacktrace.INTERNAL_EXCEPTIONS);
        getAllowUnknownTraits().convention(false);

        // By default, the build classpath and model discovery classpaths are empty file collections.
        getBuildClasspath().set(getProject().files());
        getModelDiscoveryClasspath().set(getProject().files());

        // if the smithyCli configuration exists use it by default
        if (getProject().getConfigurations().findByName(SmithyUtils.SMITHY_CLI_CONFIGURATION_NAME) != null) {
            getCliClasspath().convention(getProject().getConfigurations()
                    .getByName(SmithyUtils.SMITHY_CLI_CONFIGURATION_NAME));
        }
    }

    /**
     * Base classpath used for executing the smithy cli.
     *
     * <p>Note: this classpath must contain the smithy-cli jar.
     */
    @Classpath
    public abstract Property<FileCollection> getCliClasspath();

    /**
     * Sets whether to fail a Smithy CLI task if an unknown trait is encountered.
     *
     * <p> Defaults to {@code false}
     *
     * @return flag indicating state of allowUnknownTraits setting
     */
    @Input
    @Optional
    public abstract Property<Boolean> getAllowUnknownTraits();

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

    /**
     * Sets the detail to include in stack traces.
     *
     * <p>Defaults to {@code ShowStacktrace.INTERNAL_EXCEPTIONS}
     *
     * @return stack trace setting.
     */
    @Input
    @Optional
    public abstract Property<ShowStacktrace> getShowStackTrace();

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
     * Read-only property that returns a worker executor to use for executing a CLI
     * command.
     *
     * @return executor to use for CLI command.
     */
    @Internal
    WorkerExecutor getExecutor() {
        return getServices().get(WorkerExecutor.class);
    }

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
    protected void executeCliProcess(String command,
                                 List<String> additionalArgs,
                                 FileCollection sources,
                                 boolean disableModelDiscovery
    ) {
        List<String> args = new ArrayList<>();
        args.add(command);

        if (!getModelDiscoveryClasspath().get().isEmpty()) {
            args.add("--discover-classpath");
            args.add(getModelDiscoveryClasspath().get().getAsPath());
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
    protected void configureLoggingOptions(final List<String> args) {
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
                args.add("--quiet");
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
