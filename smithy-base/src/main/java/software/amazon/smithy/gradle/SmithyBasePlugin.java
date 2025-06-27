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
import software.amazon.smithy.gradle.tasks.SmithySelectTask;

/**
 * A {@link org.gradle.api.Plugin} that builds and validates Smithy models.
 */
public final class SmithyBasePlugin implements Plugin<Project> {
    /**
     * Default name to use for the {@link SmithyBuildTask} task created by this plugin.
     */
    public static final String SMITHY_BUILD_TASK_NAME = "smithyBuild";
    /**
     * Default name to use for the {@link SmithySelectTask} task created by this plugin.
     */
    public static final String SMITHY_SELECT_TASK_NAME = "select";
    /**
     * Default name to use for the {@link SmithyFormatTask}  task created by this plugin.
     */
    public static final String SMITHY_FORMAT_TASK_NAME = "smithyFormat";

    private static final GradleVersion MINIMUM_GRADLE_VERSION = GradleVersion.version("8.2.0");

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

        configureSmithyCliConfig(project);
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

    private static void configureSmithyCliConfig(Project project) {
        // Set up Smithy-specific configurations
        Configuration smithyCliConfiguration = project.getConfigurations()
                .maybeCreate(SmithyUtils.SMITHY_CLI_CONFIGURATION_NAME);
        smithyCliConfiguration.setVisible(true);
        smithyCliConfiguration.setDescription("Configuration for Smithy CLI and associated dependencies.");
    }

    private void configureSourceSetDefaults(Project project, SmithyExtension extension) {
        project.getExtensions().getByType(SourceSetContainer.class).all(sourceSet -> {
            createConfigurations(sourceSet, project.getConfigurations());
            SmithySourceDirectorySet sds = registerSourceSets(sourceSet, extension);
            addSelectTaskForSourceSet(sourceSet, sds, extension);
            TaskProvider<SmithyBuildTask> buildTaskTaskProvider = addBuildTaskForSourceSet(sourceSet, sds, extension);
            // Ensure smithy-build is executed as part of building the "main" feature
            if (SourceSet.isMain(sourceSet)) {
                // The CLI configuration should extend the main runtimeClasspath config, so we can
                // resolve the cli version to use based on dependencies.
                Configuration runtimeClasspathConfig = project.getConfigurations()
                        .getByName(sourceSet.getRuntimeClasspathConfigurationName());
                SmithyUtils.getCliConfiguration(project).extendsFrom(runtimeClasspathConfig);
                project.getTasks().getByName("build").dependsOn(buildTaskTaskProvider);
            }
        });

        project.afterEvaluate(p -> {
            // Resolve the Smithy CLI artifact
            CliDependencyResolver.resolve(p);

            p.getExtensions().getByType(SourceSetContainer.class).all(sourceSet -> {
                // Add format task for source set if enabled
                if (extension.getFormat().get()) {
                    SmithySourceDirectorySet sds = sourceSet.getExtensions().getByType(SmithySourceDirectorySet.class);
                    addFormatTaskForSourceSet(sourceSet, sds, extension);
                }
            });
        });
    }

    /**
     * Creates and configures smithy-specific configurations if they do not already exist.
     *
     * @param sourceSet source set to add configurations to
     * @param configurations configuration container
     */
    private void createConfigurations(SourceSet sourceSet, ConfigurationContainer configurations) {
        String compileOnlyConfigurationName = sourceSet.getCompileOnlyConfigurationName();

        Configuration smithyBuildConfig = configurations.maybeCreate(
                SmithyUtils.getSmithyBuildConfigurationName(sourceSet));
        smithyBuildConfig.setVisible(false);
        smithyBuildConfig.setTransitive(true);
        smithyBuildConfig.extendsFrom(configurations.getByName(compileOnlyConfigurationName));
        smithyBuildConfig.setDescription("Build-time smithy dependencies for " + sourceSet + ".");
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
        String taskName = SmithyUtils.getRelativeSourceSetName(sourceSet, SMITHY_FORMAT_TASK_NAME);
        TaskProvider<SmithyFormatTask> smithyFormat = project.getTasks().register(taskName, SmithyFormatTask.class,
                formatTask -> {
                    formatTask.getModels().set(sds.getSourceDirectories());
                    formatTask.setEnabled(extension.getFormat().get());
                    formatTask.getOutputs().upToDateWhen(s -> true);
                });

        // Smithy files should be formatted before they are built
        if (SourceSet.isMain(sourceSet)) {
            project.getTasks().getByName(SMITHY_BUILD_TASK_NAME).dependsOn(smithyFormat);
        }
    }

    private void addSelectTaskForSourceSet(SourceSet sourceSet, SmithySourceDirectorySet sds,
                                           SmithyExtension extension
    ) {
        String taskName = SmithyUtils.getRelativeSourceSetName(sourceSet, SMITHY_SELECT_TASK_NAME);
        String runtimeConfigName = sourceSet.getRuntimeClasspathConfigurationName();
        project.getTasks().register(taskName, SmithySelectTask.class, selectTask -> {
            selectTask.setDescription("Selects smithy models in " + sourceSet.getName() + " source set.");
            selectTask.getAllowUnknownTraits().set(extension.getAllowUnknownTraits());
            selectTask.getModels().set(sds.getSourceDirectories());
            selectTask.getFork().set(extension.getFork());
            selectTask.getCliClasspath().set(project.getConfigurations()
                    .getByName(SmithyUtils.SMITHY_CLI_CONFIGURATION_NAME));
            selectTask.getModelDiscoveryClasspath().set(project.getConfigurations()
                    .getByName(runtimeConfigName));
        });
    }

    private TaskProvider<SmithyBuildTask> addBuildTaskForSourceSet(SourceSet sourceSet,
                                                                   SmithySourceDirectorySet sds,
                                                                   SmithyExtension extension
    ) {
        String taskName = SmithyUtils.getRelativeSourceSetName(sourceSet, SMITHY_BUILD_TASK_NAME);
        String buildConfigName = SmithyUtils.getSmithyBuildConfigurationName(sourceSet);
        String runtimeConfigName = sourceSet.getRuntimeClasspathConfigurationName();

        return project.getTasks()
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

                            // Add smithy configurations as classpaths for build task
                            build.getCliClasspath().set(project.getConfigurations()
                                    .getByName(SmithyUtils.SMITHY_CLI_CONFIGURATION_NAME));
                            build.getBuildClasspath().set(project.getConfigurations()
                                    .getByName(buildConfigName));
                            build.getModelDiscoveryClasspath().set(
                                    project.getConfigurations().getByName(runtimeConfigName));

                            // this allows the main smithy build task to show up when running `gradle tasks`
                            build.setGroup(LifecycleBasePlugin.BUILD_GROUP);
                        });
    }
}
