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
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSetContainer;
import software.amazon.smithy.cli.SmithyCli;

/**
 * General utility methods used throughout the plugin.
 */
public final class SmithyUtils {

    public static final String SMITHY_PROJECTIONS = "smithyprojections";
    private static final String MAIN_SOURCE_SET = "main";
    private static final String SMITHY_SOURCE_SET_EXTENSION = "smithy";
    private static final String SOURCE_SETS_PROPERTY = "sourceSets";
    private static final Logger LOGGER = Logger.getLogger(SmithyUtils.class.getName());

    private SmithyUtils() {}

    /**
     * Gets the path to a projection plugins output.
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
    public static SourceDirectorySet getSmithyModelSources(Project project) {
        return getSmithySourceDirectory(project, MAIN_SOURCE_SET);
    }

    @SuppressWarnings("unchecked")
    private static SourceDirectorySet getSmithySourceDirectory(Project project, String name) {
        // Grab a list of all the files and directories to mark as sources.
        return (SourceDirectorySet) project.getConvention()
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

    /**
     * Executes the Smithy CLI in a separate thread or process.
     *
     * @param project Gradle project being built.
     * @param arguments CLI arguments.
     * @param classpath Classpath to use when running the CLI. Uses buildScript when not defined.
     */
    public static void executeCli(Project project, List<String> arguments, FileCollection classpath) {
        FileCollection resolveClasspath = resolveCliClasspath(project, classpath);
        boolean fork = project.getExtensions().getByType(SmithyExtension.class).getFork();
        LOGGER.fine(String.format("Executing Smithy CLI in a %s: %s; using classpath %s",
                                  fork ? "process" : "thread",
                                  String.join(" ", arguments),
                                  resolveClasspath.getAsPath()));
        if (fork) {
            executeCliProcess(project, arguments, resolveClasspath);
        } else {
            executeCliThread(arguments, resolveClasspath);
        }
    }

    public static File resolveOutputDirectory(File setOnTask, SmithyExtension extension, Project project) {
        if (setOnTask != null) {
            return setOnTask;
        } else if (extension.getOutputDirectory() != null) {
            return extension.getOutputDirectory();
        } else {
            return SmithyUtils.getProjectionOutputDir(project);
        }
    }

    private static void executeCliProcess(Project project, List<String> arguments, FileCollection classpath) {
        project.javaexec(t -> {
            t.setArgs(arguments);
            t.setClasspath(classpath);
            t.setMain(SmithyCli.class.getCanonicalName());
        });
    }

    @SuppressWarnings("unchecked")
    private static void executeCliThread(List<String> arguments, FileCollection classpath) {
        try {
            // Create a custom class loader to run within the context of.
            Set<File> files = classpath.getFiles();
            URL[] paths = new URL[files.size()];
            int i = 0;
            for (File file : files) {
                paths[i++] = file.toURI().toURL();
            }
            // Need to run this in a doPriveleged to pass SpotBugs.
            URLClassLoader classLoader = AccessController.doPrivileged(
                    (PrivilegedExceptionAction<URLClassLoader>) () -> new URLClassLoader(paths));

            // Reflection is used to make calls on the loaded SmithyCli object.
            String cliName = SmithyCli.class.getCanonicalName();
            Thread thread = new Thread(() -> {
                try {
                    Class cliClass = classLoader.loadClass(cliName);
                    Object cli = cliClass.getDeclaredMethod("create").invoke(null);
                    cliClass.getDeclaredMethod("classLoader", ClassLoader.class).invoke(cli, classLoader);
                    cliClass.getDeclaredMethod("run", List.class).invoke(cli, arguments);
                } catch (ReflectiveOperationException e) {
                    LOGGER.severe("Reflection error: " + e);
                    throw new RuntimeException(e);
                }
            });

            // Configure the thread to re-throw exception and use our custom class loader.
            thread.setContextClassLoader(classLoader);
            ExceptionHandler handler = new ExceptionHandler();
            thread.setUncaughtExceptionHandler(handler);
            thread.start();
            thread.join();
            if (handler.e != null) {
                LOGGER.severe("Enception handler: " + handler.e);
                throw handler.e;
            }

            classLoader.close();

        } catch (Throwable e) {
            throw new GradleException("Error running Smithy CLI (thread): " + e, e);
        }
    }

    private static final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        volatile Throwable e;

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            this.e = e;
        }
    }

    private static FileCollection resolveCliClasspath(Project project, FileCollection cliClasspath) {
        if (cliClasspath == null) {
            cliClasspath = SmithyUtils.getBuildscriptClasspath(project);
        }

        // Add the CLI classpath if it's missing from the given classpath.
        if (!cliClasspath.getAsPath().contains("smithy-cli")) {
            LOGGER.fine("Adding CLI classpath to command");
            cliClasspath = cliClasspath.plus(SmithyUtils.getSmithyCliClasspath(project));
        }

        return cliClasspath;
    }
}
