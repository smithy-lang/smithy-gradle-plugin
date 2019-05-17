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

package software.amazon.smithy.gradle;

import java.io.File;
import java.nio.file.Path;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSetContainer;

/**
 * General utility methods used throughout the plugin.
 */
public final class SmithyUtils {

    public static final String SMITHY_PROJECTIONS = "smithyprojections";
    private static final String MAIN_SOURCE_SET = "main";
    private static final String SMITHY_SOURCE_SET_EXTENSION = "smithy";
    private static final String SOURCE_SETS_PROPERTY = "sourceSets";

    private SmithyUtils() {}

    /**
     * Gets the path toa  projection plugins output.
     *
     * @param project Project to inspect.
     * @param projection Projection name.
     * @param plugin Plugin name.
     * @return Returns the resolved path.
     */
    public static Path getProjectionPluginPath(Project project, String projection, String plugin) {
        return project.getBuildDir().toPath()
                .resolve(SMITHY_PROJECTIONS)
                .resolve(project.getName())
                .resolve(projection)
                .resolve(plugin);
    }

    /**
     * Gets the source sets of a project.
     *
     * @param project Project to inspect.
     * @return Returns the project's source sets.
     */
    public static SourceSetContainer getSourceSets(Project project) {
        return (SourceSetContainer) project.getProperties().get(SOURCE_SETS_PROPERTY);
    }

    /**
     * Gets the Smithy model sources of the "main" source set.
     *
     * @param project Project to inspect.
     * @return Returns the Smithy model sources.
     */
    public static FileCollection getSmithyModelSources(Project project) {
        return getSmithySourceDirectory(project, MAIN_SOURCE_SET);
    }

    @SuppressWarnings("unchecked")
    private static FileCollection getSmithySourceDirectory(Project project, String name) {
        return (FileCollection) project.getConvention()
                .getPlugin(JavaPluginConvention.class)
                .getSourceSets()
                .getByName(name)
                .getExtensions()
                .getByName(SMITHY_SOURCE_SET_EXTENSION);
    }

    /**
     * Gets the classpath used with the "smithyCli" configuration.
     *
     * @param project Project to inspect.
     * @return Returns the Smithy CLI classpath used to run the CLI.
     */
    public static FileCollection getSmithyCliClasspath(Project project) {
        return getClasspath(project, "smithyCli");
    }

    /**
     * Gets the classpath of a project by name.
     *
     * @param project Project to inspect.
     * @param configurationName Name of the classpath to retrieve.
     * @return Returns the classpath.
     */
    public static FileCollection getClasspath(Project project, String configurationName) {
        return project.getConfigurations().getByName(configurationName);
    }

    /**
     * Gets the buildscript classpath of a project.
     *
     * @param project Project to inspect.
     * @return Returns the classpath.
     */
    public static FileCollection getBuildscriptClasspath(Project project) {
        return project.getBuildscript().getConfigurations().getByName("classpath");
    }

    /**
     * Gets the path to the temp directory where Smithy model resources are placed
     * in the generated JAR of a project.
     *
     * @param project Project to inspect.
     * @return Returns the classpath.
     */
    public static File getSmithyResourceTempDir(Project project) {
        Path metaInf = project.getBuildDir().toPath().resolve("tmp").resolve("smithy-inf");
        return metaInf.resolve("META-INF").resolve("smithy").toFile();
    }

    /**
     * Gets the path to the default output directory of projections.
     *
     * @param project Project to inspect.
     * @return Returns the default output directory.
     */
    public static File getProjectionOutputDir(Project project) {
        return project.getProjectDir().toPath()
                .resolve("build")
                .resolve(SMITHY_PROJECTIONS)
                .resolve(project.getName())
                .toFile();
    }
}
