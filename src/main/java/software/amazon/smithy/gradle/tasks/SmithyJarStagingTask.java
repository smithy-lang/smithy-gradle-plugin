/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.tasks;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import software.amazon.smithy.gradle.SmithyUtils;


public abstract class SmithyJarStagingTask extends DefaultTask {
    private static final String SOURCES_PLUGIN_NAME = "sources";
    private static final String SOURCE_PROJECTION = "source";

    // Set up defaults
    @Inject
    public SmithyJarStagingTask() {
        getProjection().convention(SOURCE_PROJECTION);
        getBaseBuildDir().convention(getProject().getLayout().getBuildDirectory());
    }

    @InputDirectory
    public abstract DirectoryProperty getInputDirectory();

    @Input
    public abstract Property<String> getProjection();

    @InputDirectory
    @Optional
    public abstract DirectoryProperty getBaseBuildDir();

    @Internal
    Provider<Path> getSourcesPluginPath() {
        return getInputDirectory().getAsFile().zip(getProjection(), (input, projection) ->
                SmithyUtils.getProjectionPluginPath(input, projection, SOURCES_PLUGIN_NAME));
    }

    @Internal
    Provider<File> getSmithyResourceTempDir() {
        return getBaseBuildDir().getAsFile()
                .map(base -> SmithyUtils.getSmithyResourceTempDir(getName(), base));
    }

    @OutputDirectory
    public Provider<File> getSmithyStagingDir() {
         return getSmithyResourceTempDir()
                 .map(File::getParentFile)
                 .map(File::getParentFile);
    }

    @TaskAction
    public void copyModelsToStaging() {
        getLogger().info("Copying smithy models to staging");
        Path sources = getSourcesPluginPath().get();
        validateSources(sources);

        getProject().copy(c -> {
            c.from(sources.toFile());
            c.into(getSmithyResourceTempDir().get());
        });
    }

    private void validateSources(final Path sources) {
        if (!Files.isDirectory(sources)) {
            if (getProjection().get().equals("source")) {
                getLogger().warn("No Smithy model files were found");
            } else {
                // This means the projection was explicitly set, so fail if no models were found.
                throw new GradleException("Smithy projection `" + getProjection().get() + "` not found or does not "
                        + "contain any models. Is this projection defined in your "
                        + "smithy-build.json file?");
            }
        }
    }
}
