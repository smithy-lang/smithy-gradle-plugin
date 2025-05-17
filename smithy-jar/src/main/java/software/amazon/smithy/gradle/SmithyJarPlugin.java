/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import java.io.File;
import java.util.List;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.language.jvm.tasks.ProcessResources;
import software.amazon.smithy.gradle.actions.SmithyManifestUpdateAction;
import software.amazon.smithy.gradle.tasks.SmithyBuildTask;
import software.amazon.smithy.gradle.tasks.SmithyJarStagingTask;
import software.amazon.smithy.gradle.tasks.SmithyValidateTask;
import software.amazon.smithy.utils.ListUtils;

/**
 * A {@link org.gradle.api.Plugin} that adds Smithy models to a generated Jar.
 */
public class SmithyJarPlugin implements Plugin<Project> {
    private static final String STAGING_TASK_NAME = "smithyJarStaging";
    private static final String VALIDATE_JAR_TASK_NAME = "smithyJarValidate";
    private static final List<String> PREREQUISITE_PLUGINS = ListUtils.of(
            "java",
            "java-library",
            "android",
            "android-library",
            "org.jetbrains.kotlin.jvm",
            "org.jetbrains.kotlin.android",
            "scala"
    );
    private static final List<String> SUPPORTED_LANGUAGES = ListUtils.of("java", "kotlin", "scala");
    private boolean wasApplied = false;
    private SmithyExtension extension;

    private final Project project;

    @Inject
    public SmithyJarPlugin(Project project) {
        this.project = project;
    }

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(SmithyBasePlugin.class);

        extension = project.getExtensions().getByType(SmithyExtension.class);

        for (String pluginId : PREREQUISITE_PLUGINS) {
            project.getPluginManager().withPlugin(pluginId, this::applyWithPlugin);
        }

        project.afterEvaluate(p -> {
            if (!wasApplied) {
                throw new GradleException("The smithy-jar plugin could not be applied during project evaluation."
                        + " A Java, Kotlin, or android plugin must be applied to the project first.");
            }
        });
    }

    private void applyWithPlugin(AppliedPlugin appliedPlugin) {
        if (wasApplied) {
            project.getLogger().info("The smithy-jar plugin was already applied to the project: "
            + project.getPath());
            return;
        }
        wasApplied = true;

        project.getExtensions().getByType(SourceSetContainer.class).forEach(sourceSet -> {
            // Only stage jar task for main component
            if (SourceSet.isMain(sourceSet)) {
                SmithyBuildTask buildTask = project.getTasks().withType(SmithyBuildTask.class)
                        .getByName(SmithyBasePlugin.SMITHY_BUILD_TASK_NAME);
                // Must execute after project has evaluated or else the java "enabled" setting will not be resolved
                project.afterEvaluate(p -> addJavaTasksForSourceSet(sourceSet, buildTask));
            }
        });
    }

    private void addJavaTasksForSourceSet(SourceSet sourceSet, SmithyBuildTask buildTask) {
        Task jarTask = project.getTasks().getByName(sourceSet.getJarTaskName());

        // Set up staging task
        TaskProvider<SmithyJarStagingTask> jarStagingTaskProvider = project.getTasks()
                .register(STAGING_TASK_NAME, SmithyJarStagingTask.class, stagingTask -> {
                    stagingTask.mustRunAfter(buildTask);
                    stagingTask.getInputDirectory().set(buildTask.getOutputDir());
                    stagingTask.getProjection().set(extension.getSourceProjection());
                    // Only enable this task if the jar task and build task are also enabled.
                    stagingTask.setEnabled(jarTask.getEnabled() && buildTask.getEnabled());
                });
        jarTask.dependsOn(jarStagingTaskProvider);

        // Include Smithy models and the generated manifest in the JAR by adding them to the resources source set.
        File metaInf = jarStagingTaskProvider.get().getSmithyMetaInfDir().get();
        project.getLogger().debug("Registering Smithy resource artifacts with Java resources: {}", metaInf);
        SourceDirectorySet metaInfSrcDir = sourceSet.getResources().srcDir(metaInf);

        // This plugin supports loading Smithy models from various locations, including
        // META-INF/smithy. It also creates a staging directory for all the merged
        // resources that were found in each search location. This can cause conflicts
        // between the META-INF/smithy files and staging directory, so we need to
        // ignore duplicate conflicts.
        ProcessResources process = project.getTasks().withType(ProcessResources.class).getByName("processResources");
        process.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
        process.dependsOn(jarStagingTaskProvider);

        // Ensure the smithy files generated for the JAR are available for any Compile tasks so smithy-generated
        // data can be picked up by annotation processors and compile tasks
        for (String lang : SUPPORTED_LANGUAGES) {
            AbstractCompile compileTask = project.getTasks().withType(AbstractCompile.class)
                    .findByName(sourceSet.getCompileTaskName(lang));
            if (compileTask != null) {
                // Ensures staging occurs before compilation so smithy files are available
                compileTask.dependsOn(process);
                compileTask.setClasspath(compileTask.getClasspath().plus(metaInfSrcDir.getSourceDirectories()));
            }
        }

        // Update manifest with smithy build info and source tags
        jarTask.doFirst("updateJarManifest",
                new SmithyManifestUpdateAction(project, extension.getTags().get()));

        TaskProvider<SmithyValidateTask> validateTaskProvider = project.getTasks()
                .register(VALIDATE_JAR_TASK_NAME, SmithyValidateTask.class, validateTask -> {
                    validateTask.dependsOn(jarTask);

                    // Only enable validation if the jar Task is also enabled
                    validateTask.setEnabled(jarTask.getEnabled());
                    validateTask.getSources().set(jarTask.getOutputs().getFiles());
                    validateTask.getAllowUnknownTraits().set(extension.getAllowUnknownTraits());

                    // Add to verification group, so this tasks shows up in the output of `gradle tasks`
                    validateTask.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);
                    validateTask.getOutputs().upToDateWhen(s -> true);
                });
        project.getTasks().getByName("test").dependsOn(validateTaskProvider);
    }
}
