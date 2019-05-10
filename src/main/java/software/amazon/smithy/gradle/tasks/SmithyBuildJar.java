/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.gradle.tasks;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import software.amazon.smithy.gradle.SmithyUtils;

/**
 * Executes SmithyBuild in order to update the JAR of a project.
 */
public class SmithyBuildJar extends SmithyBuild {

    @TaskAction
    public void build() {
        super.build();
        createSourceSets(getSmithyBuildOutput());
        copySourceModels();
        copyModelsToStaging();
    }

    // Create source sets for every projection + plugin combination.
    private void createSourceSets(Path output) {
        for (Path projection : getDirectories(output)) {
            String projectionName = output.relativize(projection).toString();

            // Add a source set for the entire projection.
            String projectionSourceSetName = "smithy_" + projectionName;
            getProject().getLogger().info("Creating Smithy projection source set: " + projectionSourceSetName);
            SmithyUtils.getSourceSets(getProject()).create(projectionSourceSetName, sourceSet -> {
                sourceSet.resources(sds -> sds.srcDir(projection.toFile()));
            });

            // Add a source set for each plugin in the projection.
            for (Path plugin : getDirectories(projection)) {
                String pluginName = projection.relativize(plugin).toString();
                String pluginSourceSetName = projectionSourceSetName + "_" + pluginName;
                getProject().getLogger().info("Creating Smithy plugin source set: " + pluginSourceSetName);
                SmithyUtils.getSourceSets(getProject()).create(pluginSourceSetName, sourceSet -> {
                    sourceSet.resources(sds -> sds.srcDir(plugin.toFile()));
                });
            }
        }
    }

    private List<Path> getDirectories(Path path) {
        try {
            return Files.list(path).filter(Files::isDirectory).collect(Collectors.toList());
        } catch (IOException e) {
            throw new GradleException(e.getMessage(), e);
        }
    }

    // Copies models found in this project to "smithymodel".
    private void copySourceModels() {
        try {
            File sources = SmithyUtils.getProjectionPluginPath(getProject(), "source", "sources").toFile();
            String projectName = getProject().getName();
            File dest = getProject().getBuildDir().toPath().resolve("smithymodel").resolve(projectName).toFile();
            FileUtils.copyDirectory(sources, dest);
            FileUtils.deleteQuietly(dest.toPath().resolve("manifest").toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // Copy the "sources" plugin output to the generated resources directory.
    private void copyModelsToStaging() {
        String projection = getProjection();
        Path sources = SmithyUtils.getProjectionPluginPath(getProject(), projection, "sources");
        try {
            FileUtils.copyDirectory(sources.toFile(), SmithyUtils.getSmithyResourceTempDir(getProject()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
