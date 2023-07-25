/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.gradle.api.GradleException;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
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
 * Gradle configuration settings for Smithy.
 */
public abstract class SmithyExtension {
    private static final String SMITHY_BUILD_CONFIG_DEFAULT = "smithy-build.json";
    private static final String SMITHY_SOURCE_PROJECTION_DEFAULT = "source";
    private static final String OUTPUT_DIRECTORY = "outputDirectory";

    private final NamedDomainObjectContainer<SmithySourceDirectorySet> sourceSets;

    public SmithyExtension(Project project) {
        // set defaults
        getSmithyBuildConfigs().convention(project.files(SMITHY_BUILD_CONFIG_DEFAULT));
        getSourceProjection().convention(SMITHY_SOURCE_PROJECTION_DEFAULT);
        getFork().convention(false);
        getFormat().convention(true);
        getAllowUnknownTraits().convention(false);
        getOutputDirectory().convention(getDefaultOutputDirectory(project));

        ObjectFactory objectFactory = project.getObjects();
        this.sourceSets = objectFactory.domainObjectContainer(SmithySourceDirectorySet.class,
                name -> objectFactory.newInstance(DefaultSmithySourceDirectorySet.class,
                        objectFactory.sourceDirectorySet(name, name + " Smithy sources"))
        );
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
     * Gets a custom collection of smithy-build.json files to use when
     * building the model.
     *
     * @return Returns the collection of build configurations.
     */
    public abstract Property<FileCollection> getSmithyBuildConfigs();


    /**
     * Gets the projection name in use by the extension as the source (primary) projection.
     *
     * @return Returns the projection name and defaults to "source".
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
     * Gets whether to execute the format task on files in the Smithy source set
     *
     * <p>By default formatting is run on all `.smithy` files in the Smithy
     * source set.
     */
    public abstract Property<Boolean> getFormat();

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
    public abstract Property<File> getOutputDirectory();

    @Internal
    private Provider<File> getDefaultOutputDirectory(final Project project) {
        return getSmithyBuildConfigs()
                .flatMap(FileCollection::getElements)
                .map(fileSystemLocations -> fileSystemLocations.stream()
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
                        .orElse(null)
                ).map(project::file);
    }

    private static Optional<String> parseOutputDirFromBuildConfig(Path buildConfigPath) {
        // The smithy-build.json file does not necessarily exist. If it does not exist
        // do not try to read the config information
        if (!Files.exists(buildConfigPath)) {
            return Optional.empty();
        }
        return Node.parse(IoUtils.readUtf8File(buildConfigPath))
                .expectObjectNode()
                .getMember(OUTPUT_DIRECTORY)
                .map(Node::expectStringNode)
                .map(StringNode::getValue);
    }
}
