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

import java.util.ArrayList;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Optional;
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.Nullable;
import software.amazon.smithy.cli.SmithyCli;
import software.amazon.smithy.gradle.SmithyExtension;
import software.amazon.smithy.gradle.SmithyUtils;
import software.amazon.smithy.model.traits.DynamicTrait;
import software.amazon.smithy.utils.ListUtils;

/**
 * Abstract class used to share functionality across Smithy tasks.
 */
abstract class SmithyTask extends DefaultTask {

    private String projection = "source";
    private FileCollection smithyBuildConfigs;
    private FileCollection models;
    private FileCollection classpath;
    private FileCollection modelDiscoveryClasspath;
    private boolean allowUnknownTraits;

    /**
     * Updates the task to use settings from the given Smithy extension.
     *
     * @param extension Smithy extension to update the task with.
     */
    public void updateWithExtension(SmithyExtension extension) {
        setProjection(extension.getProjection());
        setSmithyBuildConfigs(extension.getSmithyBuildConfigs());
        setClasspath(extension.getClasspath());
        setModelDiscoveryClasspath(extension.getModelDiscoveryClasspath());
        setAllowUnknownTraits(extension.getAllowUnknownTraits());
    }

    /**
     * Gets the name of the projection being applied in the build.
     *
     * @return Returns the name of the projection.
     */
    @Input @Optional
    public final String getProjection() {
        return projection;
    }

    /**
     * Sets the name of the projection being applied.
     *
     * @param projection Name of the projection to set.
     */
    public final void setProjection(String projection) {
        this.projection = projection;
    }

    /**
     * Gets whether or not this is the source projection.
     *
     * @return Returns true if projection is "source".
     */
    @Internal
    public boolean isSourceProjection() {
        return getProjection().equals("source");
    }

    /**
     * Gets the smithy-build.json files set on the task.
     *
     * @return Returns the resolved collection of configurations.
     */
    @InputFiles @Optional
    final FileCollection getSmithyBuildConfigs() {
        return java.util.Optional.ofNullable(smithyBuildConfigs)
                .orElseGet(() -> getProject().files("smithy-build.json"));
    }

    /**
     * Sets a custom collection of smithy-build.json files to use when
     * building the model.
     *
     * @param smithyBuildConfigs Sets the collection of build configurations.
     */
    public final void setSmithyBuildConfigs(FileCollection smithyBuildConfigs) {
        this.smithyBuildConfigs = smithyBuildConfigs;
    }

    /**
     * Gets the list of custom models to validate.
     *
     * @return Returns the custom models to validate.
     */
    @InputFiles @Optional @Nullable
    public final FileCollection getModels() {
        return models;
    }

    /**
     * Sets the list of custom models to validate.
     *
     * @param models The custom models to validate.
     */
    public final void setModels(FileCollection models) {
        this.models = models;
    }

    /**
     * Gets the classpath used when loading models, traits, validators, etc.
     *
     * @return Returns the nullable classpath in use.
     */
    @Classpath @Optional
    public final FileCollection getClasspath() {
        return classpath;
    }

    /**
     * Sets the classpath to use when loading models, traits, validators, etc.
     *
     * @param classpath Classpath to use.
     */
    public final void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    /**
     * Gets the classpath used for model discovery.
     *
     * @return Returns the nullable classpath in use.
     */
    @Classpath @Optional
    public final FileCollection getModelDiscoveryClasspath() {
        return modelDiscoveryClasspath;
    }

    /**
     * Sets the classpath to use for model discovery.
     *
     * @param modelDiscoveryClasspath Classpath to use for model discovery.
     */
    public final void setModelDiscoveryClasspath(FileCollection modelDiscoveryClasspath) {
        this.modelDiscoveryClasspath = modelDiscoveryClasspath;
    }

    /**
     * Gets whether or not unknown traits in the model should be ignored.
     *
     * <p>By default, the build will fail if unknown traits are encountered.
     * This can be set to true to allow unknown traits to pass through the
     * model and be loaded as a {@link DynamicTrait}.
     *
     * @return Returns true if unknown traits are allowed.
     */
    @Input @Optional
    public final boolean getAllowUnknownTraits() {
        return allowUnknownTraits;
    }

    /**
     * Sets whether or not unknown traits are ignored.
     *
     * @param allowUnknownTraits Set to true to ignore unknown traits.
     */
    public final void setAllowUnknownTraits(boolean allowUnknownTraits) {
        this.allowUnknownTraits = allowUnknownTraits;
    }

    /**
     * Executes the given CLI command.
     *
     * <p>This method will take care of adding --discover, --discover-classpath,
     * and --allow-unknown-traits.
     *
     * @param command The command to execute.
     * @param customArguments Custom arguments that aren't one of the shared args.
     * @param cliClasspath Classpath to use when running the CLI.
     * @param modelDiscoveryClasspath Classpath to use for model discovery.
     */
    final void executeCliProcess(
            String command,
            List<String> customArguments,
            FileCollection cliClasspath,
            FileCollection modelDiscoveryClasspath
    ) {
        List<String> args = new ArrayList<>();
        args.add(command);

        if (getAllowUnknownTraits()) {
            args.add("--allow-unknown-traits");
        }

        if (modelDiscoveryClasspath != null) {
            args.add("--discover-classpath");
            args.add(modelDiscoveryClasspath.getAsPath());
        } else {
            args.add("--discover");
        }

        args.addAll(customArguments);

        java.util.Optional.ofNullable(getModels()).ifPresent(models -> {
            args.add("--");
            models.forEach(file -> args.add(file.getAbsolutePath()));
        });

        FileCollection resolveClasspath = resolveCliClasspath(getProject(), cliClasspath);
        getProject().getLogger().debug("Executing `smithy {}` with: {} | and classpath of {}",
                command, String.join(" ", args), resolveClasspath);

        getProject().javaexec(t -> {
            t.setArgs(args);
            t.setClasspath(resolveClasspath);
            t.setMain(SmithyCli.class.getCanonicalName());
            t.setJvmArgs(ListUtils.of("-XX:TieredStopAtLevel=2"));
        });
    }

    // Add the CLI classpath if it's missing from the given classpath.
    private static FileCollection resolveCliClasspath(Project project, FileCollection cliClasspath) {
        if (!cliClasspath.getAsPath().contains("smithy-cli")) {
            project.getLogger().debug("Adding CLI classpath to command");
            cliClasspath = cliClasspath.plus(SmithyUtils.getSmithyCliClasspath(project));
        }

        return cliClasspath;
    }
}
