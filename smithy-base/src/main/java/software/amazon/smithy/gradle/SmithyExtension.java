/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Internal;
import software.amazon.smithy.gradle.internal.DefaultSmithySourceDirectorySet;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.StringNode;
import software.amazon.smithy.model.traits.DynamicTrait;
import software.amazon.smithy.utils.IoUtils;


/**
 * Gradle configuration settings for Smithy plugins.
 */
public abstract class SmithyExtension {
    private static final String SMITHY_BUILD_CONFIG_DEFAULT = "smithy-build.json";
    private static final String SMITHY_SOURCE_PROJECTION_DEFAULT = "source";
    private static final String OUTPUT_DIRECTORY = "outputDirectory";

    private final NamedDomainObjectContainer<SmithySourceDirectorySet> sourceSets;

    @Inject
    public SmithyExtension(Project project, ObjectFactory objectFactory) {
        this.sourceSets = objectFactory.domainObjectContainer(SmithySourceDirectorySet.class,
                name -> objectFactory.newInstance(DefaultSmithySourceDirectorySet.class,
                        objectFactory.sourceDirectorySet(name, name + " Smithy sources"))
        );
        configureDefaults(project);
    }

    private void configureDefaults(Project project) {
        getSmithyBuildConfigs().convention(project.files(SMITHY_BUILD_CONFIG_DEFAULT));
        getSourceProjection().convention(SMITHY_SOURCE_PROJECTION_DEFAULT);
        getFork().convention(false);
        getFormat().convention(true);
        getAllowUnknownTraits().convention(false);
        getOutputDirectory().convention(getDefaultOutputDirectory(project));
    }

    /**
     * Collection of {@link org.gradle.api.file.SourceDirectorySet} associated with the {@link SmithyExtension}.
     *
     * @return container containing {@code SourceDirectorySet}s associated with {@link SmithyExtension}.
     */
    public NamedDomainObjectContainer<SmithySourceDirectorySet> getSourceSets() {
        return this.sourceSets;
    }


    /**
     * Gets whether to execute the format task on files in the Smithy source set
     *
     * <p>By default formatting is run on all `.smithy` files in the Smithy
     * source set.
     */
    public abstract Property<Boolean> getFormat();

    /**
     * Gets a collection of smithy-build.json files to use when
     * building the model.
     *
     * @return Returns the collection of build configurations.
     */
    public abstract Property<FileCollection> getSmithyBuildConfigs();


    /**
     * Gets the projection name in use by the extension as the source (primary) projection.
     *
     * @return Returns the projection name. Defaults to "source".
     */
    public abstract Property<String> getSourceProjection();

    /**
     * Get the tags that are searched for in classpaths when determining which
     * models are projected into the created JAR.
     *
     * <p>This plugin will look through the JARs in the buildscript classpath
     * to see if they contain a META-INF/MANIFEST.MF attribute named
     * "Smithy-Tags" that matches any of the given projection source tags.
     * The Smithy models found in each matching JAR are copied into the
     * JAR being projected. This allows a projection JAR to aggregate models
     * into a single JAR.
     *
     * @return Returns the tags. This will never return null.
     */
    public abstract SetProperty<String> getProjectionSourceTags();

    /**
     * Get the tags that are added to the JAR.
     *
     * <p>These tags are placed in the META-INF/MANIFEST.MF attribute named
     * "Smithy-Tags" as a comma separated list. JARs with Smithy-Tags can be
     * queried when building projections so that the Smithy models found in
     * each matching JAR are placed into the projection JAR.
     *
     * @return Returns the Smithy-Tags values that will be added to the created JAR.
     */
    public abstract SetProperty<String> getTags();

    /**
     * Gets whether unknown traits in the model should be ignored.
     *
     * <p>By default, the build will fail if unknown traits are encountered.
     * This can be set to true to allow unknown traits to pass through the
     * model and be loaded as a {@link DynamicTrait}.
     *
     * @return Returns true if unknown traits are allowed.
     */
    public abstract Property<Boolean> getAllowUnknownTraits();

    /**
     * Gets whether to fork when running the Smithy CLI.
     *
     * <p>By default, the CLI is run in the same process as Gradle,
     * but inside a thread with a custom class loader. This should
     * work in most cases, but there is an option to run inside a
     * process if necessary.
     *
     * @return Returns true if the CLI should fork.
     */
    public abstract Property<Boolean> getFork();

    /**
     * Gets the output directory for running Smithy build.
     *
     * @return Returns the output directory.
     */
    public abstract DirectoryProperty getOutputDirectory();

    /**
     * Convenience method to get the directory containing plugin artifacts.
     *
     * @param projection projection name
     * @param plugin name of plugin to get artifact directory for
     *
     * @return path to plugin artifact directory
     */
    public Provider<Path> getPluginProjectionPath(String projection, String plugin) {
        return getOutputDirectory().getAsFile()
                .map(file -> SmithyUtils.getProjectionPluginPath(file, projection, plugin));
    }

    /**
     * Gets the default directory to write smithy outputs to.
     *
     * <p>If an output directory is defined in the smithy-build.json then that is
     * used. Otherwise, a smithyprojections output directory under the default gradle
     * build directory is used.
     *
     * @param project gradle project
     * @return provider that returns the default output directory
     */
    @Internal
    private Provider<Directory> getDefaultOutputDirectory(final Project project) {
        return getSmithyBuildConfigs()
                .flatMap(FileCollection::getElements)
                .map(SmithyExtension::getOutputDirFromSmithyBuild)
                .map(project::file)
                .flatMap(file -> project.getLayout().getBuildDirectory().dir(file.getPath()))
                .orElse(SmithyUtils.getProjectionOutputDirProperty(project));
    }

    private static String getOutputDirFromSmithyBuild(Set<FileSystemLocation> fileSystemLocations) {
        return fileSystemLocations.stream()
                .map(FileSystemLocation::getAsFile)
                .map(File::toPath)
                .map(SmithyExtension::parseOutputDirFromBuildConfig)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce((a, b) -> {
                    throw new GradleException(
                            "Conflicting output directories defined in provided smithy build configs: "
                                    + a + ", " + b);
                })
                .orElse(null);
    }

    private static Optional<String> parseOutputDirFromBuildConfig(Path buildConfigPath) {
        // The smithy-build.json file does not necessarily exist. If it does not exist
        // do not try to read the config information
        if (!Files.exists(buildConfigPath)) {
            return Optional.empty();
        }
        return Node.parseJsonWithComments(IoUtils.readUtf8File(buildConfigPath))
                .expectObjectNode()
                .getMember(OUTPUT_DIRECTORY)
                .map(Node::expectStringNode)
                .map(StringNode::getValue);
    }
}
