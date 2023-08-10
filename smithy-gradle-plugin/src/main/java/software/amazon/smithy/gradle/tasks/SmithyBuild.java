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
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import software.amazon.smithy.gradle.SmithyUtils;

/**
 * This task allows Smithy's build CLI to be run using ad-hoc
 * settings and classpaths.
 *
 * <p>See the {@link SmithyBuildJar} task for building JARs for
 * projects.
 */
public abstract class SmithyBuild extends SmithyCliTask {

    private FileCollection smithyBuildConfigs;

    public SmithyBuild() {
        getOutputDir().convention(SmithyUtils.outputDirectory(getProject()));
    }

    /**
     * Gets the output directory for running Smithy build.
     *
     * @return Returns the output directory, lazily evaluated.
     */
    @OutputDirectory
    abstract DirectoryProperty getOutputDir();

    /**
     * Gets the output directory for running Smithy build.
     *
     * @return Returns the output directory, eagerly evaluated.
     */
    @Internal
    @Deprecated
    public File getOutputDirectory() {
        return getOutputDir().get().getAsFile();
    }

    /**
     * Sets the output directory of running smithy build.
     *
     * <p>This is the root directory where artifacts are written.
     *
     * @param outputDirectory Output directory to set.
     */
    @Deprecated
    public void setOutputDirectory(File outputDirectory) {
        getOutputDir().fileValue(outputDirectory);
    }

    /**
     * Gets the {@code smithy-build.json} files set on the task.
     *
     * @return Returns the resolved collection of configurations.
     */
    @InputFiles
    @Optional
    public final FileCollection getSmithyBuildConfigs() {
        return java.util.Optional.ofNullable(smithyBuildConfigs)
                .orElseGet(() -> getProject().files("smithy-build.json"));
    }

    /**
     * Sets a collection of {@code smithy-build.json} files to use when
     * building the model.
     *
     * <p>These configuration files are combined together and can
     * cross-reference each other in things like {@code apply} transforms.
     *
     * @param smithyBuildConfigs Sets the collection of build configurations.
     */
    public final void setSmithyBuildConfigs(FileCollection smithyBuildConfigs) {
        this.smithyBuildConfigs = smithyBuildConfigs;
    }

    @TaskAction
    public void execute() {
        // Clear out the build directory when rebuilding.
        getProject().delete(getOutputDir());

        List<String> customArgs = new ArrayList<>();

        getSmithyBuildConfigs().forEach(file -> {
            if (file.exists()) {
                getLogger().debug("Adding configuration file to CLI: {}", file);
                customArgs.add("--config");
                customArgs.add(file.getAbsolutePath());
            }
        });

        customArgs.add("--output");
        customArgs.add(getOutputDir().getAsFile().get().toString());
        executeCliProcess("build", customArgs, getClasspath(), getModelDiscoveryClasspath());
    }
}
