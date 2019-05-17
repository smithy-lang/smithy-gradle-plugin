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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.text.StyledTextOutput;
import org.gradle.internal.logging.text.StyledTextOutputFactory;
import software.amazon.smithy.gradle.SmithyExtension;
import software.amazon.smithy.gradle.SmithyUtils;

/**
 * Builds Smithy model projections and artifacts for the JAR.
 *
 * <p>A package that does not use a {@code projection} is built using both the
 * compileClasspath and buildscript classpath. Source models from the
 * project are copied literally into the generated JAR. Also included
 * in the JAR are models found in the sources of the package itself (that is,
 * model/ and src/main/smithy/). After generating the JAR, validation is run
 * using the runtimeClasspath to ensure that the generated model is correct
 * and has configured its dependencies correctly.
 *
 * <p>A package that uses a projection executes Smithy's build process using
 * the buildscript classpath and *does not* use the compileClasspath at all.
 * This serves to isolate downstream consumers of the package from internal
 * models, but it also means that dependencies may need to be specified both
 * in the buildscript and in the normal dependencies block.
 *
 * <p>This is meant to be an internal task that's automatically executed.
 */
public class SmithyBuildJar extends SmithyBuild {
    private static final String SMITHY_VALIDATE_TASK = "smithyValidate";
    private static final String RUNTIME_CLASSPATH = "runtimeClasspath";
    private static final String COMPILE_CLASSPATH = "compileClasspath";

    @TaskAction
    public void build() throws IOException {
        writeHeading("Running smithy build");

        // Clear out the directories when rebuilding.
        getProject().delete(getSmithyResourceStagingDir().getParentFile().getParentFile());

        // Configure default values that take the projection setting into account.
        configureDefaults();

        super.build();

        // Copy generated files where they're needed and register source sets.
        createSourceSets(getOutputDirectory().toPath());

        if (getProject().getTasks().getByName("jar").getEnabled()) {
            copyModelsToStaging();
            addValidationTask();
        }
    }

    @OutputDirectory
    public File getSmithyResourceStagingDir() {
        Path metaInf = getProject().getBuildDir().toPath().resolve("tmp").resolve("smithy-inf");
        return metaInf.resolve("META-INF").resolve("smithy").toFile();
    }

    private void configureDefaults() {
        if (getClasspath() == null) {
            if (isSourceProjection()) {
                setClasspath(SmithyUtils.getClasspath(getProject(), COMPILE_CLASSPATH)
                        .plus(SmithyUtils.getBuildscriptClasspath(getProject())));
            } else {
                setClasspath(SmithyUtils.getBuildscriptClasspath(getProject()));
            }
        }

        if (getModelDiscoveryClasspath() == null && isSourceProjection()) {
            setModelDiscoveryClasspath(SmithyUtils.getClasspath(getProject(), COMPILE_CLASSPATH));
        }
    }

    // Create source sets for every projection + plugin combination.
    private void createSourceSets(Path output) throws IOException {
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

    private List<Path> getDirectories(Path path) throws IOException {
        return Files.list(path).filter(Files::isDirectory).collect(Collectors.toList());
    }

    // Copy the "sources" plugin output to the generated resources directory.
    private void copyModelsToStaging() throws IOException {
        Path sources = SmithyUtils.getProjectionPluginPath(getProject(), getProjection(), "sources");
        FileUtils.copyDirectory(sources.toFile(), SmithyUtils.getSmithyResourceTempDir(getProject()));
    }

    // Only execute validation if smithy-build ran and a JAR is being created.
    private void addValidationTask() {
        Validate validateTask = getProject().getTasks().create(SMITHY_VALIDATE_TASK, Validate.class, task -> {
            task.updateWithExtension(getProject().getExtensions().getByType(SmithyExtension.class));
            task.setClasspath(SmithyUtils.getClasspath(getProject(), RUNTIME_CLASSPATH));
            // Use only model discovery with the built JAR + the runtime classpath when validating.
            FileCollection validationCp = getProject().getTasks().getByName("jar").getOutputs().getFiles()
                    .plus(SmithyUtils.getClasspath(getProject(), RUNTIME_CLASSPATH));
            task.setModelDiscoveryClasspath(validationCp);
        });

        getProject().getTasks().getByName("assemble").doLast(t -> {
            writeHeading("Validating the created JAR containing Smithy models");
            validateTask.execute();
        });
    }

    private void writeHeading(String text) {
        StyledTextOutput output = getServices().get(StyledTextOutputFactory.class)
                .create("smithy")
                .style(StyledTextOutput.Style.Header);

        if (!isSourceProjection()) {
            text += " using the `" + getProjection() + "` projection";
        }

        output.println(text);
    }
}
