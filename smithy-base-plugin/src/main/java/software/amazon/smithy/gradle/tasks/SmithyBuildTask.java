/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import software.amazon.smithy.cli.BuildParameterBuilder;
import software.amazon.smithy.gradle.SmithyUtils;
import software.amazon.smithy.gradle.internal.ProjectionArtifactDirectory;

/**
 * Executes the Smithy CLI {@code build} command.
 *
 * <p>This task will build all projections specified in the smithy-build configs provided
 * as task inputs.
 *
 */
public abstract class SmithyBuildTask extends AbstractSmithyCliTask {
    private final NamedDomainObjectContainer<ProjectionArtifactDirectory> projections;

    @Inject
    public SmithyBuildTask(ObjectFactory objectFactory) {
        super(objectFactory);

        getSourceProjection().convention("source");
        getOutputDir().convention(SmithyUtils.getProjectionOutputDirProperty(getProject()));

        this.projections = objectFactory.domainObjectContainer(
                ProjectionArtifactDirectory.class,
                name -> objectFactory.newInstance(
                        ProjectionArtifactDirectory.class, name, objectFactory)
        );
    }


    /**
     * Tags that are searched for in classpaths when determining which
     * models are projected into the created JAR.
     *
     * <p>This plugin will look through the JARs in the discovery classpath
     * to see if they contain a META-INF/MANIFEST.MF attribute named
     * "Smithy-Tags" that matches any of the given projection source tags.
     * The Smithy models found in each matching JAR are copied into the
     * JAR being projected. This allows a projection JAR to aggregate models
     * into a single JAR.
     *
     */
    @Input
    @Optional
    public abstract SetProperty<String> getProjectionSourceTags();


    /** Smithy build configs to use for building models.
     *
     * @return list of smithy-build config json files
     */
    @Input
    public abstract Property<FileCollection> getSmithyBuildConfigs();


    /** Sets whether to fail a {@link SmithyBuildTask} if an unknown trait is encountered.
     *
     * <p> Defaults to {@code true}
     *
     * @return flag indicating state of allowUnknownTraits setting
     */
    @Input
    @Optional
    public abstract Property<Boolean> getAllowUnknownTraits();


    /**
     * Projection to treat as the "source" or primary projection.
     */
    @Input
    @Optional
    public abstract Property<String> getSourceProjection();


    /**
     * Output directory for Smithy build artifacts.
     */
    @OutputDirectory
    @Optional
    public abstract DirectoryProperty getOutputDir();

    /** Read-only property.
     *
     * @return list of absolute paths of model files.
     */
    @Internal
    List<String> getModelAbsolutePaths() {
        return getModels().get().getFiles().stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }


    /**
     * Collection of Projection artifact directories.
     *
     * <p>Note: this will only contain values when the smithyBuild task is enabled
     *
     * @return container containing named {@code }. Projection names will be the
     * same as specified in the smithy-build configs used for a smithy-build task.
     *
     */
    @Internal
    public NamedDomainObjectContainer<ProjectionArtifactDirectory> getProjections() {
        return this.projections;
    }

    /**
     * Convenience method to get the directory containing projection artifacts.
     *
     * @param projectionName name of projection to get directory for
     *
     * @return directory containing projection artifacts
     */
    public DirectoryProperty getProjectionDirectory(String projectionName) {
        return this.projections.getByName(projectionName).getDirectory();
    }

    /**
     * Convenience method to get the directory containing plugin artifacts.
     *
     * @param projectionName projection to search for plugin artifacts
     * @param pluginName name of plugin to get directory for
     *
     * @return directory containing plugin artifacts
     */
    public DirectoryProperty getPluginDirectory(String projectionName, String pluginName) {
        return this.projections.getByName(projectionName)
                .getPlugins().getByName(pluginName).getDirectory();
    }


    @TaskAction
    public void execute() {
        writeHeading("Running smithy build");

        BuildParameterBuilder builder = new BuildParameterBuilder();

        // Model discovery classpath
        builder.libClasspath(getDiscoveryClasspath().get().getAsPath());
        builder.buildClasspath(getCliExecutionClasspath().get().getAsPath());
        builder.projectionSourceTags(getProjectionSourceTags().get());
        builder.allowUnknownTraits(getAllowUnknownTraits().get());
        builder.output(getOutputDir().getAsFile().get().getAbsolutePath());
        builder.projectionSource(getSourceProjection().get());

        getSmithyBuildConfigs().get().forEach(config -> builder.addConfigIfExists(config.getAbsolutePath()));

        if (getModels().isPresent()) {
            builder.addSourcesIfExists(getModelAbsolutePaths());
        }

        builder.discover(true);

        // Add extra configuration options for build command
        List<String> extraArgs = new ArrayList<>();
        configureLoggingOptions(extraArgs);
        builder.addExtraArgs(extraArgs.toArray(new String[0]));

        BuildParameterBuilder.Result result = builder.build();
        getLogger().debug("Executing smithy build with arguments: " + result.args);
        SmithyUtils.executeCli(getExecutor(),
                result.args,
                getCliExecutionClasspath().get(),
                getFork().get()
        );

        addOutputsToArtifactContainer();
    }

    private void addOutputsToArtifactContainer() {
        Path output = getOutputDir().getAsFile().get().toPath();
        for (Path projectionDirPath : getDirectories(output)) {
            // Add all projection artifacts to the object container.
            projections.register(getRelativeName(output, projectionDirPath), projection -> {
                projection.getDirectory().set(projectionDirPath.toFile());

                // Add all plugins in the projection to the projection container
                for (Path pluginDirPath : getDirectories(projectionDirPath)) {
                    projection.getPlugins().register(getRelativeName(projectionDirPath, pluginDirPath),
                            pluginContainer -> pluginContainer.getDirectory().set(pluginDirPath.toFile()));
                }
            });
        }
    }

    private static List<Path> getDirectories(Path path) {
        List<Path> result;
        try (Stream<Path> paths = Files.list(path)) {
            result = paths.filter(Files::isDirectory).collect(Collectors.toList());
        } catch (IOException exc) {
            throw new UncheckedIOException(exc);
        }
        return result;
    }

    private static String getRelativeName(Path root, Path nested) {
        return root.relativize(nested).toString();
    }
}
