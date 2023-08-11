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
import org.gradle.api.internal.artifacts.configurations.ConfigurationRoles;
import org.gradle.api.internal.artifacts.configurations.RoleBasedConfigurationContainerInternal;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JvmEcosystemPlugin;
import org.gradle.api.plugins.ReportingBasePlugin;
import org.gradle.api.plugins.jvm.internal.JvmPluginServices;
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

    // The plugin uses gradle API features from 8.2.0
    private static final GradleVersion MINIMUM_GRADLE_VERSION = GradleVersion.version("8.2.0");
    private static final GradleVersion MIN_SMITHY_FORMAT_VERSION = GradleVersion.version("1.33.0");


    JvmPluginServices jvmPluginServices;
    Project project;

    @Inject
    public SmithyBasePlugin(Project project, JvmPluginServices jvmPluginServices) {
        this.jvmPluginServices = jvmPluginServices;
        this.project = project;
    }

    @Override
    public void apply(@Nonnull Project project) {
        validateGradleVersion();

        project.getPlugins().apply(BasePlugin.class);
        // Adds base SourceSetContainer
        project.getPlugins().apply(JvmEcosystemPlugin.class);
        // Allows Smithy Diff to add reports
        project.getPlugins().apply(ReportingBasePlugin.class);

        // Add smithy source set extension
        maybeCreateDefaultSourceSets(project);
        SmithyBaseExtension smithyExtension = project.getExtensions().create("smithy", SmithyBaseExtension.class);

        configureSourceSetDefaults(project, smithyExtension);
    }

    private static void maybeCreateDefaultSourceSets(Project project) {
        SourceSetContainer container = project.getExtensions().getByType(SourceSetContainer.class);
        container.maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME);
        container.maybeCreate(SourceSet.TEST_SOURCE_SET_NAME);
    }

    private static void validateGradleVersion() {
        if (GradleVersion.current().compareTo(MINIMUM_GRADLE_VERSION) < 0) {
            throw new GradleException(
                    "Current Gradle version is " + GradleVersion.current().getVersion()
                            + ". The minimum supported Gradle version for the Smithy plugin is "
                            + MINIMUM_GRADLE_VERSION.getVersion());
        }
    }

    private void configureSourceSetDefaults(Project project, SmithyBaseExtension extension) {
        project.getExtensions().getByType(SourceSetContainer.class).forEach(sourceSet -> {
            ConfigurationContainer configurations = project.getConfigurations();
            createConfigurations(sourceSet, (RoleBasedConfigurationContainerInternal) configurations);
            SmithySourceDirectorySet sds = registerSourceSets(sourceSet, extension);

            String cliVersion = CliDependencyResolver.resolve(project, sourceSet);
            if (extension.getFormat().get() && cliVersionSupportsFormat(cliVersion)) {
                addFormatTaskForSourceSet(sourceSet, sds, extension);
            }

            if (SourceSet.isMain(sourceSet)) {
                addBuildTaskForSourceSet(sourceSet, sds, extension);
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
     * Set up implementation, runtimeOnly, and RuntimeClasspath configurations if they do not exist.
     *
     * <p>**Note**: this method follows the conventions set up by the JavaBasePlugin.
     *
     * @param sourceSet source set to add configurations to
     * @param configurations configuration container
     */
    private void createConfigurations(SourceSet sourceSet, RoleBasedConfigurationContainerInternal configurations) {
        String implementationConfigurationName = sourceSet.getImplementationConfigurationName();
        String runtimeOnlyConfigurationName = sourceSet.getRuntimeOnlyConfigurationName();
        String runtimeClasspathConfigurationName = sourceSet.getRuntimeClasspathConfigurationName();
        String sourceSetName = sourceSet.toString();

        Configuration implementationConfiguration =
                configurations.maybeCreateWithRole(implementationConfigurationName, ConfigurationRoles.BUCKET, false,
                        false);
        implementationConfiguration.setVisible(false);
        implementationConfiguration.setDescription("Implementation only dependencies for " + sourceSetName + ".");

        Configuration runtimeOnlyConfiguration = configurations.maybeCreateWithRole(runtimeOnlyConfigurationName,
                ConfigurationRoles.BUCKET, false, false);
        runtimeOnlyConfiguration.setVisible(false);
        runtimeOnlyConfiguration.setDescription("Runtime only dependencies for " + sourceSetName + ".");

        Configuration runtimeClasspathConfiguration = configurations.maybeCreateWithRole(
                runtimeClasspathConfigurationName, ConfigurationRoles.RESOLVABLE, false, false);
        runtimeClasspathConfiguration.setVisible(false);
        runtimeClasspathConfiguration.setDescription("Runtime classpath of " + sourceSetName + ".");
        runtimeClasspathConfiguration.extendsFrom(runtimeOnlyConfiguration, implementationConfiguration);
        jvmPluginServices.configureAsRuntimeClasspath(runtimeClasspathConfiguration);

        sourceSet.setRuntimeClasspath(sourceSet.getOutput().plus(runtimeClasspathConfiguration));

        // Set up Smithy-specific configurations
        Configuration smithyCliConfiguration = configurations.maybeCreate(
                SmithyUtils.getSmithyCliConfigurationName(sourceSet));
        smithyCliConfiguration.extendsFrom(runtimeClasspathConfiguration);
        smithyCliConfiguration.setVisible(false);
        smithyCliConfiguration.setDescription("Configuration for Smithy CLI and associated dependencies for the "
                + sourceSetName + " sourceSet.");

        Configuration smithyBuildConfig = configurations.maybeCreate(
                SmithyUtils.getSmithyBuildConfigurationName(sourceSet));
        smithyBuildConfig.setVisible(false);
        smithyBuildConfig.setTransitive(true);
        smithyBuildConfig.extendsFrom(runtimeClasspathConfiguration);
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
    private static SmithySourceDirectorySet registerSourceSets(SourceSet sourceSet, SmithyBaseExtension extension) {
        // Add the smithy source set as an extension
        SmithySourceDirectorySet sds = extension.getSourceSets().create(sourceSet.getName());
        sourceSet.getExtensions().add(SmithySourceDirectorySet.NAME, sds);
        SmithySourceDirectorySet.SOURCE_DIRS.forEach(sourceDir ->
                sds.srcDir(sourceDir.replace("$name", sourceSet.getName())));
        sds.include("**/*.smithy");

        return sds;
    }

    private void addFormatTaskForSourceSet(SourceSet sourceSet, SmithySourceDirectorySet sds,
                                           SmithyBaseExtension extension
    ) {
        // Set up format task and Register all smithy sourceSets as formatting targets
        String taskName = SmithyUtils.getRelativeSourceSetName(sourceSet, "smithyFormat");
        TaskProvider<SmithyFormatTask> smithyFormat = project.getTasks().register(taskName, SmithyFormatTask.class,
                formatTask -> {
                    formatTask.getModels().set(sds.getSourceDirectories());
                    formatTask.setEnabled(extension.getFormat().get());
                });

        if (SourceSet.isMain(sourceSet)) {
            project.getTasks().getByName("build").dependsOn(smithyFormat);
        }
    }

    private void addBuildTaskForSourceSet(SourceSet sourceSet,
                                          SmithySourceDirectorySet sds,
                                          SmithyBaseExtension extension
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

        if (SourceSet.isMain(sourceSet)) {
            project.getTasks().getByName("build").dependsOn(buildTaskTaskProvider);
        }
    }
}
