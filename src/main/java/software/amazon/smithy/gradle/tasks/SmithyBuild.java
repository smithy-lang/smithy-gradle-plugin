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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import software.amazon.smithy.gradle.SmithyExtension;
import software.amazon.smithy.gradle.SmithyUtils;

/**
 * Builds Smithy model projections and artifacts.
 */
public class SmithyBuild extends SmithyTask {

    private File outputDirectory;

    /**
     * Gets the output directory for running Smithy build.
     *
     * @return Returns the output directory.
     */
    @OutputDirectory
    public File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * Sets the output directory of running Smithy Build.
     *
     * @param outputDirectory Output directory to set.
     */
    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    @Override
    public void updateWithExtension(SmithyExtension extension) {
        super.updateWithExtension(extension);
        setOutputDirectory(extension.getOutputDirectory());
    }

    @TaskAction
    public void build() throws IOException {
        // Clear out the build directory when rebuilding.
        getProject().delete(getOutputDirectory());

        List<String> customArgs = new ArrayList<>();
        Optional.ofNullable(getSmithyBuildConfigs()).ifPresent(files -> files.forEach(file -> {
            if (file.exists()) {
                customArgs.add("--config");
                customArgs.add(file.getAbsolutePath());
            }
        }));

        customArgs.add("--output");
        customArgs.add(getOutputDirectory().toString());

        FileCollection resolvedClasspath = Optional.ofNullable(getClasspath())
                .orElseGet(() -> SmithyUtils.getBuildscriptClasspath(getProject()));

        executeCliProcess("build", customArgs, resolvedClasspath, getModelDiscoveryClasspath());
    }
}
