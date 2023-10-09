/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.util.GradleVersion;
import software.amazon.smithy.gradle.internal.CliDependencyResolver;
import software.amazon.smithy.gradle.tasks.SmithyBuildTask;
import software.amazon.smithy.gradle.tasks.SmithyFormatTask;

/**
 * A {@link org.gradle.api.Plugin} that builds and validates Smithy models.
 */
public final class SmithyBasePlugin implements Plugin<Project> {
    public static final String SMITHY_BUILD_TASK_NAME = "smithyBuild";

    private static final GradleVersion MINIMUM_GRADLE_VERSION = GradleVersion.version("8.2.0");
    private static final GradleVersion MIN_SMITHY_FORMAT_VERSION = GradleVersion.version("1.33.0");

    private final Project project;

    @Inject
    public SmithyBasePlugin(Project project) {
        this.project = project;
    }

    @Override
    public void apply(@Nonnull Project project) {
        validateGradleVersion();

        // Creates required configurations and base source set container
        project.getPlugins().apply(JavaBasePlugin.class);

        // Add smithy source set extension
        SmithyExtension smithyExtension = project.getExtensions().create("smithy", SmithyExtension.class);

        configureSourceSetDefaults(project, smithyExtension);
    }

    private static void validateGradleVersion() {
        if (GradleVersion.current().compareTo(MINIMUM_GRADLE_VERSION) < 0) {
            throw new GradleException(
                    "Current Gradle version is " + GradleVersion.current().getVersion()
                            + ". The minimum supported Gradle version for the Smithy plugin is "
                            + MINIMUM_GRADLE_VERSION.getVersion());
        }
    }

    private void configureSourceSetDefaults(Project project, SmithyExtension extension) {
        project.getExtensions().getByType(SourceSetContainer.class).all(sourceSet -> {
            createConfigurations(sourceSet, project.getConfigurations());
            SmithySourceDirectorySet sds = registerSourceSets(sourceSet, extension);
            TaskProvider<SmithyBuildTask> buildTaskTaskProvider = addBuildTaskForSourceSet(sourceSet, sds, extension);
            // Ensure smithy-build is executed as part of building the "main" feature
            if (SourceSet.isMain(sourceSet)) {
                project.getTasks().getByName("build").dependsOn(buildTaskTaskProvider);
            }

            // Add format task for source set if enabled and the CLI version supports it
            String cliVersion = CliDependencyResolver.resolve(project, sourceSet);
            if (extension.getFormat().get() && cliVersionSupportsFormat(cliVersion)) {
                addFormatTaskForSourceSet(sourceSet, sds, extension);
            }
        });
    }


    /**
     * Smithy-format was added in version 1.33.0. It is not supported in earlier CLI versions.
     */
    private boolean cliVersionSupportsFormat(String cliVersion) {
        boolean supported = GradleVersion.version(cliVersion).compareTo(MIN_SMITHY_FORMAT_VERSION) >= 0;

        if (!supported) {
            project.getLogger().warn("Formatting task is not supported Smithy CLI version: "
                    + "(" + cliVersion + "). Skipping."
                    + "Minimum supported Smithy CLI version for formatting is "
                    + MIN_SMITHY_FORMAT_VERSION);
        }

        return supported;
    }


    /**
     * Creates and configures smithy configurations if they do not already exist.
     **
     * @param sourceSet source set to add configurations to
     * @param configurations configuration container
     */
    private void createConfigurations(SourceSet sourceSet, ConfigurationContainer configurations) {
        String runtimeClasspathConfigurationName = sourceSet.getRuntimeClasspathConfigurationName();
        String compileOnlyConfigurationName = sourceSet.getCompileOnlyConfigurationName();
        String sourceSetName = sourceSet.toString();

        // Set up Smithy-specific configurations
        Configuration smithyCliConfiguration = configurations.maybeCreate(
                SmithyUtils.getSmithyCliConfigurationName(sourceSet));
        smithyCliConfiguration.extendsFrom(configurations.getByName(runtimeClasspathConfigurationName));
        smithyCliConfiguration.setVisible(false);
        smithyCliConfiguration.setDescription("Configuration for Smithy CLI and associated dependencies for the "
                + sourceSetName + " sourceSet.");

        Configuration smithyBuildConfig = configurations.maybeCreate(
                SmithyUtils.getSmithyBuildConfigurationName(sourceSet));
        smithyBuildConfig.setVisible(false);
        smithyBuildConfig.setTransitive(true);
        smithyBuildConfig.extendsFrom(configurations.getByName(compileOnlyConfigurationName));
        smithyBuildConfig.setDescription("Build-time smithy dependencies for " + sourceSetName + ".");
    }

    /**
     * Registers a custom Smithy source set as an extension of a given source set.
     *
     * <p>Smithy models can be placed in {@code model/}, {@code src/$sourceSetName/smithy},
     * {@code src/$sourceSetName/smithy}, {@code src/$sourceSetName/resources/META-INF/smithy}, and
     * {@code src/$sourceSetName/resources/META-INF/smithy}. This code will add these
     * directories to the specified source set as a "smithy" extension.
     *
     * @param sourceSet root sourceSet to add extensions to
     */
    private static SmithySourceDirectorySet registerSourceSets(SourceSet sourceSet, SmithyExtension extension) {
        // Add the smithy source set as an extension
        SmithySourceDirectorySet sds = extension.getSourceSets().create(sourceSet.getName());
        sourceSet.getExtensions().add(SmithySourceDirectorySet.NAME, sds);
        SmithySourceDirectorySet.SOURCE_DIRS.forEach(sourceDir ->
                sds.srcDir(sourceDir.replace("$name", sourceSet.getName())));
        sds.include("**/*.smithy");

        return sds;
    }

    private void addFormatTaskForSourceSet(SourceSet sourceSet, SmithySourceDirectorySet sds,
                                           SmithyExtension extension
    ) {
        // Set up format task and Register all smithy sourceSets as formatting targets
        String taskName = SmithyUtils.getRelativeSourceSetName(sourceSet, "smithyFormat");
        TaskProvider<SmithyFormatTask> smithyFormat = project.getTasks().register(taskName, SmithyFormatTask.class,
                formatTask -> {
                    formatTask.getModels().set(sds.getSourceDirectories());
                    formatTask.setEnabled(extension.getFormat().get());
                });

        // Smithy files should be formatted before they are built
        if (SourceSet.isMain(sourceSet)) {
            project.getTasks().getByName(SMITHY_BUILD_TASK_NAME).dependsOn(smithyFormat);
        }
    }

    private TaskProvider<SmithyBuildTask> addBuildTaskForSourceSet(SourceSet sourceSet,
                                                                   SmithySourceDirectorySet sds,
                                                                   SmithyExtension extension
    ) {
        String taskName = SmithyUtils.getRelativeSourceSetName(sourceSet, SMITHY_BUILD_TASK_NAME);
        TaskProvider<SmithyBuildTask> buildTaskTaskProvider = project.getTasks()
                .register(taskName, SmithyBuildTask.class,
                        build -> {
                            // Configure basic extension settings
                            build.setDescription("Builds Smithy models for " + sourceSet.getName() + " source set.");
                            build.getAllowUnknownTraits().set(extension.getAllowUnknownTraits());
                            build.getModels().set(sds.getSourceDirectories());
                            build.getFork().set(extension.getFork());
                            build.getSmithyBuildConfigs().set(extension.getSmithyBuildConfigs());
                            build.getSourceProjection().set(extension.getSourceProjection());
                            build.getProjectionSourceTags().set(extension.getProjectionSourceTags());
                            build.getOutputDir().set(extension.getOutputDirectory());

                            // this allows the main smithy build task to show up when running `gradle tasks`
                            build.setGroup(LifecycleBasePlugin.BUILD_GROUP);
                        });

        return buildTaskTaskProvider;
    }
}
