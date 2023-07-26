/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import java.io.File;
import java.util.List;
import javax.annotation.Nonnull;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.jvm.tasks.ProcessResources;
import org.gradle.util.GradleVersion;
import software.amazon.smithy.gradle.actions.SmithyManifestUpdateAction;
import software.amazon.smithy.gradle.internal.CliDependencyResolver;
import software.amazon.smithy.gradle.tasks.SmithyBuildTask;
import software.amazon.smithy.gradle.tasks.SmithyFormatTask;
import software.amazon.smithy.gradle.tasks.SmithyJarStagingTask;
import software.amazon.smithy.gradle.tasks.SmithyValidateTask;
import software.amazon.smithy.utils.ListUtils;


/**
 * Applies the Smithy plugin to Gradle.
 */
public final class SmithyPlugin implements Plugin<Project> {
    private static final String BUILD_CONFIG_TYPE = "smithyBuildDep";
    private static final String BUILD_TASK_NAME = "smithyBuild";
    private static final GradleVersion MIN_SMITHY_FORMAT_VERSION = GradleVersion.version("1.33.0");
    private static final GradleVersion MINIMUM_GRADLE_VERSION = GradleVersion.version("6.0");

    // At least one of these extensions must be applied before the smithy plugin
    // is applied. This is required because these plugins create the required
    // source sets and configurations for the plugin
    private static final List<String> PREREQUISITE_PLUGINS = ListUtils.of(
            "java",
            "java-library",
            "android",
            "android-library",
            "org.jetbrains.kotlin.jvm",
            "org.jetbrains.kotlin.android"
    );

    private Project project;
    private SmithyExtension smithyExtension;

    @Override
    public void apply(@Nonnull Project project) {
        this.project = project;

        // Perform plugin validations
        validateGradleVersion();
        validatePrereqs();

        // Register the Smithy extension so that tasks can be configured.
        smithyExtension = project.getExtensions().create("smithy", SmithyExtension.class);
        validateExtension(smithyExtension);

        // resolve and configure smithy-cli dependencies
        // These are all added to a configuration named `smithyCli`
        // Note: the `smithyCli` configuration extends from runtime,
        // so it will include all runtime deps
        String cliVersion = CliDependencyResolver.resolve(project);

        // Register a smithy sourceSet as an extension of the main source set
        project.getExtensions().getByType(SourceSetContainer.class).forEach(sourceSet -> {
            SmithySourceDirectorySet sds = registerSourceSets(sourceSet);
            createSmithyBuildConfiguration(project, sourceSet);

            // Must execute after project has evaluated or else the format setting will not be resolved
            if (smithyExtension.getFormat().get() && cliVersionSupportsFormat(cliVersion)) {
                addFormatTaskForSourceSet(sourceSet, sds);
            }

            if (sourceSet.getName().equals(SourceSet.MAIN_SOURCE_SET_NAME)) {
                TaskProvider<SmithyBuildTask> buildProvider = addBuildTaskForSourceSet(sourceSet, sds);

                // Must execute after project has evaluated or else the java "enabled" setting will not be resolved
                project.afterEvaluate(projectAfterEvaluation -> {
                    projectAfterEvaluation.getPluginManager().withPlugin("java", plugin -> {
                        addJavaTasksForSourceSet(sourceSet, buildProvider);
                    });
                });
            }
        });
    }


    private static void validateGradleVersion() {
        if (GradleVersion.current().compareTo(MINIMUM_GRADLE_VERSION) < 0) {
            throw new GradleException(
                    "Current Gradle version is " + GradleVersion.current().getVersion()
                            + ". The minimum supported Gradle version for the Smithy plugin is "
                            + MINIMUM_GRADLE_VERSION.getVersion());
        }
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

    private void validatePrereqs() {
        boolean prerequisitesApplied = PREREQUISITE_PLUGINS.stream().anyMatch(
                pluginName -> project.getPluginManager().hasPlugin(pluginName)
        );

        if (!prerequisitesApplied) {
            throw new GradleException("Smithy plugin could not be applied. "
                    + "Please apply at least one of the prerequisite plugins from the following list before "
                    + "applying the smithy plugin: " + PREREQUISITE_PLUGINS);
        }
    }

    private static void validateExtension(SmithyExtension smithyExtension) {
        // Set source projection settings
        // We can only set source projections for projections other than source.
        if (!smithyExtension.getProjectionSourceTags().isPresent()
                && smithyExtension.getSourceProjection().get().equals("source")) {
            throw new GradleException("Projection source tags set with source projection. "
                    + "Set a value for the `sourceProjection` that matches a projection defined "
                    + "in one of the smithy-build configs for this project.");
        }
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
    private SmithySourceDirectorySet registerSourceSets(SourceSet sourceSet) {
        // Add the smithy source set as an extension
        SmithySourceDirectorySet sds = smithyExtension.getSourceSets().create(sourceSet.getName());
        sourceSet.getExtensions().add(SmithySourceDirectorySet.NAME, sds);
        SmithySourceDirectorySet.SOURCE_DIRS.forEach(sourceDir ->
                sds.srcDir(sourceDir.replace("$name", sourceSet.getName())));
        sds.include("**/*.smithy");

        return sds;
    }

    /**
     * Creates a 'smithyBuild' configuration for the given source set.
     *
     * <p>This configures the model discovery classpath. Users can set dependencies
     * on this configuration that they want included during the build step, but not
     * included as dependencies of their jars.
     *
     */
    private Configuration createSmithyBuildConfiguration(Project project, SourceSet sourceSet) {
        String configName = SmithyUtils.getRelativeSourceSetName(sourceSet.getName(), BUILD_CONFIG_TYPE);
        Configuration config = project.getConfigurations().create(configName);

        config.setDescription("Build-time smithy dependencies for " + sourceSet.getName() + ".");
        config.setVisible(false);

        // Declared Smithy build deps will pull in transitive dependencies as build deps too
        config.setTransitive(true);

        // Discovery classpath includes all runtime classpath configurations as well
       config.extendsFrom(project.getConfigurations().getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME));

        return config;
    }

    private TaskProvider<SmithyBuildTask> addBuildTaskForSourceSet(SourceSet sourceSet, SmithySourceDirectorySet sds) {

        String taskName = SmithyUtils.getRelativeSourceSetName(sourceSet.getName(), BUILD_TASK_NAME);
        TaskProvider<SmithyBuildTask> buildTaskTaskProvider = project.getTasks()
                .register(taskName, SmithyBuildTask.class,
                    build -> {
                        // Configure basic extension settings
                        build.setDescription("Builds Smithy models for " + sourceSet.getName() + " source set.");
                        build.getAllowUnknownTraits().set(smithyExtension.getAllowUnknownTraits());
                        build.getModels().set(sds.getSourceDirectories());
                        build.getFork().set(smithyExtension.getFork());
                        build.getSmithyBuildConfigs().set(smithyExtension.getSmithyBuildConfigs());
                        build.getSourceProjection().set(smithyExtension.getSourceProjection());
                        build.getProjectionSourceTags().set(smithyExtension.getProjectionSourceTags());
                        build.getOutputDir().set(smithyExtension.getOutputDirectory());

                        // this allows the main smithy build task to show up when running `gradle tasks`
                        build.setGroup(LifecycleBasePlugin.BUILD_GROUP);
                    });

        if (sourceSet.getName().equals(SourceSet.MAIN_SOURCE_SET_NAME)) {
            project.getTasks().getByName("build").dependsOn(buildTaskTaskProvider);
        } else if (sourceSet.getName().equals(SourceSet.TEST_SOURCE_SET_NAME)) {
            project.getTasks().getByName("test").dependsOn(buildTaskTaskProvider);
        }

        return buildTaskTaskProvider;
    }

    private void addFormatTaskForSourceSet(SourceSet sourceSet, SmithySourceDirectorySet sds) {
        // Set up format task and Register all smithy sourceSets as formatting targets
        String taskName = SmithyUtils.getRelativeSourceSetName(sourceSet.getName(), "smithyFormat");
        TaskProvider<SmithyFormatTask> smithyFormat = project.getTasks().register(taskName, SmithyFormatTask.class,
                formatTask -> {
                    formatTask.getModels().set(sds.getSourceDirectories());
                    formatTask.setEnabled(smithyExtension.getFormat().get());
                });

        if (sourceSet.getName().equals(SourceSet.MAIN_SOURCE_SET_NAME)) {
            project.getTasks().getByName("build").dependsOn(smithyFormat);
        } else if (sourceSet.getName().equals(SourceSet.TEST_SOURCE_SET_NAME)) {
            project.getTasks().getByName("test").dependsOn(smithyFormat);
        }

    }

    private void addJavaTasksForSourceSet(SourceSet sourceSet, TaskProvider<SmithyBuildTask> buildTaskTaskProvider) {
        Task jarTask = project.getTasks().getByName(JavaPlugin.JAR_TASK_NAME);

        // Set up staging task
        TaskProvider<SmithyJarStagingTask> jarStagingTaskProvider = project.getTasks()
                .register("smithyJarStaging", SmithyJarStagingTask.class, stagingTask -> {
                    stagingTask.mustRunAfter(buildTaskTaskProvider);
                    stagingTask.getInputDirectory().set(buildTaskTaskProvider.get().getOutputDir());
                    stagingTask.getProjection().set(smithyExtension.getSourceProjection());
                    // Only enable this task if the jar task and build task are also enabled.
                    stagingTask.setEnabled(jarTask.getEnabled() && buildTaskTaskProvider.get().getEnabled());
                });
        jarTask.dependsOn(jarStagingTaskProvider);

        // Include Smithy models and the generated manifest in the JAR by adding them to the resources source set.
        File metaInf = jarStagingTaskProvider.get().getSmithyMetaInfDir().get();
        project.getLogger().debug("Registering Smithy resource artifacts with Java resources: {}", metaInf);
        sourceSet.getResources().srcDir(metaInf);


        // This plugin supports loading Smithy models from various locations, including
        // META-INF/smithy. It also creates a staging directory for all the merged
        // resources that were found in each search location. This can cause conflicts
        // between the META-INF/smithy files and staging directory, so we need to
        // ignore duplicate conflicts.
        ProcessResources task = project.getTasks().withType(ProcessResources.class).getByName("processResources");
        task.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
        task.dependsOn(jarStagingTaskProvider);


        // Update manifest with smithy build info and source tags
        jarTask.doFirst("updateJarManifest",
                new SmithyManifestUpdateAction(project, smithyExtension.getTags().get()));

        TaskProvider<SmithyValidateTask> validateTaskProvider = project.getTasks()
                .register("smithyJarValidate", SmithyValidateTask.class, validateTask -> {
                    validateTask.dependsOn(jarTask);

                    // Only enable validation if the jar Task is also enabled
                    validateTask.setEnabled(jarTask.getEnabled());

                    validateTask.getJarToValidate().set(jarTask.getOutputs().getFiles());
                    validateTask.getAllowUnknownTraits().set(smithyExtension.getAllowUnknownTraits());

                    // Add to verification group, so this tasks shows up in the output of `gradle tasks`
                    validateTask.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
                });
        project.getTasks().getByName("test").dependsOn(validateTaskProvider);

    }
}
