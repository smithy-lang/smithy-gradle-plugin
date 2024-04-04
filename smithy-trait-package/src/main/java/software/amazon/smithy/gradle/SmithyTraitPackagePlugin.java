/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import java.nio.file.Path;
import java.util.Optional;
import javax.inject.Inject;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import software.amazon.smithy.gradle.internal.CliDependencyResolver;

/**
 * A {@link org.gradle.api.Plugin} that adds sets up a package for a custom trait.
 */
public class SmithyTraitPackagePlugin implements Plugin<Project> {
    private static final String SMITHY_TRAIT_CODEGEN_DEP_NAME = "smithy-trait-codegen";
    private static final String TRAIT_CODEGEN_PLUGIN_NAME = "trait-codegen";
    private static final String DEPENDENCY_NOTATION = "software.amazon.smithy:%s:%s";

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
                // Add Trait codegen outputs to source set
                addGeneratedTraits(sourceSet);
                project.afterEvaluate(p -> configureDependencies(sourceSet));
            }
        });
    }

    private void addGeneratedTraits(SourceSet sourceSet) {
        Path pluginOutput = extension.getPluginProjectionPath(SOURCE, TRAIT_CODEGEN_PLUGIN_NAME).get();
        sourceSet.getJava().srcDir(pluginOutput);
        sourceSet.getResources().srcDir(pluginOutput).exclude("**/*.java");
    }

    // TODO: Add smithy-model dependency?
    private void configureDependencies(SourceSet sourceSet) {
        Configuration smithyBuild = project.getConfigurations()
                .getByName(SmithyUtils.getSmithyBuildConfigurationName(sourceSet));

        // Prefer explicit dependency
        Optional<Dependency> explicitDepOptional = smithyBuild.getAllDependencies().stream()
                .filter(d -> SmithyUtils.isMatchingDependency(d,
                        SmithyTraitPackagePlugin.SMITHY_TRAIT_CODEGEN_DEP_NAME))
                .findFirst();
        if (explicitDepOptional.isPresent()) {
            project.getLogger().info(String.format("(using explicitly configured Dependency for %s: %s)",
                    SmithyTraitPackagePlugin.SMITHY_TRAIT_CODEGEN_DEP_NAME, explicitDepOptional.get().getVersion()));
            return;
        }

        // If trait codegen does not exist, add the dependency with the same version as the resolved CLI version
        String cliVersion = CliDependencyResolver.resolve(project);
        project.getDependencies().add(smithyBuild.getName(),
                String.format(DEPENDENCY_NOTATION, SmithyTraitPackagePlugin.SMITHY_TRAIT_CODEGEN_DEP_NAME, cliVersion));
    }
}
