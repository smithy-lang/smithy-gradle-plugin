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
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Optional;
import software.amazon.smithy.gradle.SmithyUtils;

/**
 * Abstract class used to share functionality across Smithy CLI tasks
 * (that is, tasks that are meant to be run ad-hoc).
 */
abstract class SmithyCliTask extends BaseSmithyTask {

    private FileCollection classpath;
    private FileCollection modelDiscoveryClasspath;
    private boolean modelDiscovery;

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
        this.modelDiscovery = true;
        this.modelDiscoveryClasspath = modelDiscoveryClasspath;
    }

    /**
     * Returns true if this task uses model discovery.
     *
     * @return Returns true if model discovery is enabled.
     */
    @Classpath
    @Optional
    public final boolean getModelDiscovery() {
        return modelDiscovery;
    }

    /**
     * Sets whether or not model discovery is enabled.
     *
     * @param modelDiscovery Set to true to enable model discovery.
     */
    public final void setModelDiscovery(boolean modelDiscovery) {
        this.modelDiscovery = modelDiscovery;

        if (!modelDiscovery) {
            modelDiscoveryClasspath = null;
        }
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
        List<String> args = new ArrayList<>();
        args.add(command);

        if (getAllowUnknownTraits()) {
            args.add("--allow-unknown-traits");
        }

        if (modelDiscoveryClasspath != null) {
            args.add("--discover-classpath");
            args.add(modelDiscoveryClasspath.getAsPath());
        } else if (modelDiscovery) {
            args.add("--discover");
        }

        args.addAll(customArguments);

        java.util.Optional.ofNullable(getModels()).ifPresent(models -> {
            args.add("--");
            models.forEach(file -> args.add(file.getAbsolutePath()));
        });

        SmithyUtils.executeCliProcess(getProject(), args, cliClasspath);
    }
}
