/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import javax.inject.Inject;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.language.jvm.tasks.ProcessResources;
import software.amazon.smithy.gradle.internal.CliDependencyResolver;
import software.amazon.smithy.gradle.tasks.MergeSpiFilesTask;
import software.amazon.smithy.gradle.tasks.SmithyBuildTask;

/**
 * A {@link org.gradle.api.Plugin} that sets up a package for a custom trait.
 */
public class SmithyTraitPackagePlugin implements Plugin<Project> {
    private static final String SMITHY_TRAIT_CODEGEN_DEP_NAME = "smithy-trait-codegen";
    private static final String TRAIT_CODEGEN_PLUGIN_NAME = "trait-codegen";
    private static final String TRAIT_SPI_FILE_NAME = "software.amazon.smithy.model.traits.TraitService";
    private static final String TRAIT_SPI_FILE_PATH = "META-INF/services/" + TRAIT_SPI_FILE_NAME;
    private static final String DEPENDENCY_NOTATION = "software.amazon.smithy:%s:%s";
    private static final String MERGE_TASK_NAME = "mergeSpiFiles";
    private static final String SOURCE = "source";

    private SmithyExtension extension;
    private final Project project;

    @Inject
    public SmithyTraitPackagePlugin(Project project) {
        this.project = project;
    }

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(JavaLibraryPlugin.class);
        project.getPlugins().apply(SmithyJarPlugin.class);

        extension = project.getExtensions().getByType(SmithyExtension.class);

        // Only configure trait codegen dependency for main sourceSet
        project.getExtensions().getByType(SourceSetContainer.class).all(sourceSet -> {
            if (SourceSet.isMain(sourceSet)) {
                project.afterEvaluate(p -> configureDependencies(sourceSet));

                // Add Trait codegen outputs to source set
                Path pluginOutput = extension.getPluginProjectionPath(SOURCE, TRAIT_CODEGEN_PLUGIN_NAME).get();
                sourceSet.getJava().srcDir(pluginOutput);

                // Merge generated file with the existing SPI file if found
                Optional<File> existingOptional = existingSpiFile(sourceSet);
                if (existingOptional.isPresent()) {
                    project.getLogger().info("Found existing SPI file. Merging with generated...");
                    TaskProvider<MergeSpiFilesTask> mergeTaskProvider = addMergeTask(sourceSet, pluginOutput,
                            existingOptional.get());
                    sourceSet.getResources().srcDir(mergeTaskProvider.get().getOutputDir());

                    // remove existing from resources to prevent conflicts
                    sourceSet.getResources().exclude(e -> e.getFile().equals(existingOptional.get()));
                } else {
                    // Add the generated SPI file as a resource
                    sourceSet.getResources().srcDir(pluginOutput).exclude("**/*.java");
                }
            }
        });
    }

    private Optional<File> existingSpiFile(SourceSet sourceSet) {
        return sourceSet.getResources().getFiles().stream()
                .filter(f -> f.getName().equals(TRAIT_SPI_FILE_NAME))
                .findFirst();
    }

    private void configureDependencies(SourceSet sourceSet) {
        Configuration smithyBuild = project.getConfigurations()
                .getByName(SmithyUtils.getSmithyBuildConfigurationName(sourceSet));

        // Prefer explicit dependency
        Optional<Dependency> explicitDepOptional = smithyBuild.getAllDependencies().stream()
                .filter(d -> SmithyUtils.isMatchingDependency(d, SMITHY_TRAIT_CODEGEN_DEP_NAME))
                .findFirst();
        if (explicitDepOptional.isPresent()) {
            project.getLogger().info(String.format("(using explicitly configured Dependency for %s: %s)",
                    SMITHY_TRAIT_CODEGEN_DEP_NAME, explicitDepOptional.get().getVersion()));
            return;
        }

        // If trait codegen does not exist, add the dependency with the same version as the resolved CLI version
        String cliVersion = CliDependencyResolver.resolve(project);
        project.getDependencies().add(smithyBuild.getName(),
                String.format(DEPENDENCY_NOTATION, SMITHY_TRAIT_CODEGEN_DEP_NAME, cliVersion));
    }

    private TaskProvider<MergeSpiFilesTask> addMergeTask(SourceSet sourceSet, Path pluginPath, File existing) {
        String mergeTaskName = SmithyUtils.getRelativeSourceSetName(sourceSet, MERGE_TASK_NAME);
        SmithyBuildTask buildTask = project.getTasks().withType(SmithyBuildTask.class)
                .getByName(SmithyBasePlugin.SMITHY_BUILD_TASK_NAME);
        ProcessResources process = project.getTasks().withType(ProcessResources.class).getByName("processResources");
        Task compileTask = project.getTasks().getByName(sourceSet.getCompileJavaTaskName());
        return project.getTasks().register(mergeTaskName, MergeSpiFilesTask.class,
                mergeTask -> {
                    mergeTask.mustRunAfter(buildTask);
                    process.dependsOn(mergeTask);
                    compileTask.dependsOn(mergeTask);
                    mergeTask.getGeneratedFile().set(pluginPath.resolve(TRAIT_SPI_FILE_PATH).toFile());
                    mergeTask.getExistingFile().set(existing);
                });
    }
}
