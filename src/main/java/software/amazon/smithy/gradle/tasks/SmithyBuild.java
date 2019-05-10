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

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import software.amazon.smithy.gradle.SmithyUtils;

/**
 * Builds Smithy model projections and artifacts.
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
 * <p>This task can be run as many times and however as you like.
 */
public class SmithyBuild extends SmithyTask {

    @TaskAction
    public void build() {
        List<String> args = createCliArguments("build", determineModelDiscoveryClasspath());

        Optional.ofNullable(getSmithyBuildConfigs())
                .ifPresent(files -> files.forEach(file -> {
                    if (file.exists()) {
                        args.add("--config");
                        args.add(file.getAbsolutePath());
                    }
                }));

        args.add("--output");
        args.add(getSmithyBuildOutput().toString());
        addModelArguments(args);
        executeCliProcess(determineClasspath(), args);
    }

    @Internal
    Path getSmithyBuildOutput() {
        return getProject().getProjectDir().toPath()
                .resolve("build")
                .resolve(SmithyUtils.SMITHY_PROJECTIONS)
                .resolve(getProject().getName());
    }

    private FileCollection determineClasspath() {
        if (getClasspath() != null) {
            return getClasspath();
        } else if (getProjection().equals("source")) {
            return SmithyUtils.getClasspath(getProject(), "compileClasspath")
                    .plus(SmithyUtils.getBuildscriptClasspath(getProject()));
        } else {
            return SmithyUtils.getBuildscriptClasspath(getProject());
        }
    }

    private FileCollection determineModelDiscoveryClasspath() {
        if (getModelDiscoveryClasspath() != null) {
            return getModelDiscoveryClasspath();
        } else if (getProjection().equals("source")) {
            return SmithyUtils.getClasspath(getProject(), "compileClasspath");
        } else {
            // Run with the same classpath as the normal classpath.
            return null;
        }
    }
}
