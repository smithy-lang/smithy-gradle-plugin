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
import java.util.logging.Level;
import org.gradle.StartParameter;
import org.gradle.api.file.FileCollection;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.logging.configuration.ShowStacktrace;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.workers.WorkerExecutor;
import software.amazon.smithy.gradle.SmithyUtils;

/**
 * Abstract class used to share functionality across Smithy CLI tasks
 * (that is, tasks that are meant to be run ad-hoc).
 */
abstract class SmithyCliTask extends BaseSmithyTask {
    private FileCollection classpath;
    private FileCollection modelDiscoveryClasspath;
    private boolean disableModelDiscovery;
    private boolean addRuntimeClasspath;
    private boolean addBuildScriptClasspath;
    private boolean addCompileClasspath;

    /**
     * Gets the classpath used when loading models, traits, validators, etc.
     *
     * @return Returns the nullable classpath in use.
     */
    @Classpath
    @Optional
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
    @Classpath
    @Optional
    public final FileCollection getModelDiscoveryClasspath() {
        return modelDiscoveryClasspath;
    }

    /**
     * Sets the classpath to use for model discovery and enables model discovery.
     *
     * @param modelDiscoveryClasspath Classpath to use for model discovery.
     */
    public final void setModelDiscoveryClasspath(FileCollection modelDiscoveryClasspath) {
        this.disableModelDiscovery = false;
        this.modelDiscoveryClasspath = modelDiscoveryClasspath;
    }

    /**
     * Returns true if this task disables model discovery.
     *
     * @return Returns true if model discovery is disabled.
     */
    @Input
    public final boolean getDisableModelDiscovery() {
        return disableModelDiscovery;
    }

    /**
     * Sets whether or not model discovery is disabled.
     *
     * @param disableModelDiscovery Set to true to disable model discovery.
     */
    public final void setDisableModelDiscovery(boolean disableModelDiscovery) {
        this.disableModelDiscovery = disableModelDiscovery;

        if (disableModelDiscovery) {
            modelDiscoveryClasspath = null;
        }
    }

    /**
     * Gets whether or not the runtime classpath is used when building.
     *
     * @return Returns true if used.
     */
    @Input
    public boolean getAddRuntimeClasspath() {
        return addRuntimeClasspath;
    }

    /**
     * Sets whether or not the runtime classpath is used when building.
     *
     * @param addRuntimeClasspath Set to true to use.
     */
    public void setAddRuntimeClasspath(boolean addRuntimeClasspath) {
        this.addRuntimeClasspath = addRuntimeClasspath;
    }

    /**
     * Gets whether or not the buildScript classpath is used when building.
     *
     * @return Returns true if used.
     */
    @Input
    public boolean getAddBuildScriptClasspath() {
        return addBuildScriptClasspath;
    }

    /**
     * Sets whether or not the buildScript classpath is used when building.
     *
     * @param addBuildScriptClasspath Set to true to use.
     */
    public void setAddBuildScriptClasspath(boolean addBuildScriptClasspath) {
        this.addBuildScriptClasspath = addBuildScriptClasspath;
    }

    /**
     * Gets whether or not the compile classpath is used when building.
     *
     * @return Returns true if used.
     */
    @Input
    public boolean getAddCompileClasspath() {
        return addCompileClasspath;
    }

    /**
     * Sets whether or not the compile classpath is used when building.
     *
     * @param addCompileClasspath Set to true to use.
     */
    public void setAddCompileClasspath(boolean addCompileClasspath) {
        this.addCompileClasspath = addCompileClasspath;
    }

    /**
     * Executes the given CLI command.
     *
     * <p>This method will take care of adding --discover, --discover-classpath,
     * and --allow-unknown-traits.
     *
     * @param command The command to execute.
     * @param customArguments Custom arguments that aren't one of the shared args.
     * @param cliClasspath Classpath to use when running the CLI. Uses buildScript when not defined.
     * @param modelDiscoveryClasspath Classpath to use for model discovery.
     */
    final void executeCliProcess(
            String command,
            List<String> customArguments,
            FileCollection cliClasspath,
            FileCollection modelDiscoveryClasspath
    ) {
        // Setup the CLI classpath.
        if (getAddRuntimeClasspath() || getAddCompileClasspath() || getAddBuildScriptClasspath()) {
            FileCollection originalCliClasspath = cliClasspath;
            cliClasspath = cliClasspath != null ? cliClasspath : getProject().files();
            if (getAddBuildScriptClasspath()) {
                cliClasspath = cliClasspath.plus(SmithyUtils.getBuildscriptClasspath(getProject()));
            }
            if (getAddRuntimeClasspath()) {
                cliClasspath = cliClasspath.plus(SmithyUtils.getClasspath(getProject(), RUNTIME_CLASSPATH));
            }
            if (getAddCompileClasspath()) {
                cliClasspath = cliClasspath.plus(SmithyUtils.getClasspath(getProject(), COMPILE_CLASSPATH));
            }
            getLogger().debug(
                    "Configuring classpath: runtime: {}; compile: {}; buildscript: {}; updated from {} to {}",
                    addRuntimeClasspath, addCompileClasspath, addBuildScriptClasspath,
                    originalCliClasspath == null ? "null" : originalCliClasspath.getAsPath(),
                    cliClasspath.getAsPath());
        }

        List<String> args = new ArrayList<>();
        args.add(command);

        if (getAllowUnknownTraits()) {
            args.add("--allow-unknown-traits");
        }

        if (modelDiscoveryClasspath != null) {
            args.add("--discover-classpath");
            args.add(modelDiscoveryClasspath.getAsPath());
        } else if (!disableModelDiscovery) {
            args.add("--discover");
        }

        // Add --stacktrace and --logging settings based on Gradle's settings.
        StartParameter startParameter = getProject().getGradle().getStartParameter();

        ShowStacktrace showStacktrace = startParameter.getShowStacktrace();
        if (showStacktrace == ShowStacktrace.ALWAYS || showStacktrace == ShowStacktrace.ALWAYS_FULL) {
            args.add("--stacktrace");
        }

        LogLevel level = startParameter.getLogLevel();
        switch (level) {
            case DEBUG:
                args.add("--debug");
                break;
            case LIFECYCLE: // The default setting in Gradle, so use Smithy's default of WARNING.
            case WARN:
                args.add("--logging");
                args.add(Level.WARNING.toString());
                break;
            case QUIET:
                args.add("--logging");
                args.add(Level.OFF.toString());
                break;
            case ERROR:
                args.add("--logging");
                args.add(Level.SEVERE.toString());
                break;
            case INFO:
            default:
                args.add("--logging");
                args.add(Level.INFO.toString());
                break;
        }

        args.addAll(customArguments);

        if (!getModels().isEmpty()) {
            args.add("--");
            getModels().forEach(file -> {
                if (file.exists()) {
                    getLogger().debug("Adding Smithy model file to CLI: {}", file);
                    args.add(file.getAbsolutePath());
                } else {
                    getLogger().error("Skipping Smithy model file because it does not exist: {}", file);
                }
            });
        }

        WorkerExecutor executor = getServices().get(WorkerExecutor.class);
        SmithyUtils.executeCli(executor, getProject(), args, cliClasspath);
    }
}
