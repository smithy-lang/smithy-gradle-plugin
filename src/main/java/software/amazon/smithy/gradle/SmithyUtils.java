/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.workers.ClassLoaderWorkerSpec;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;
import software.amazon.smithy.build.SmithyBuildException;
import software.amazon.smithy.gradle.internal.CliDependencyResolver;
import software.amazon.smithy.utils.SmithyInternalApi;
import software.amazon.smithy.utils.StringUtils;

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
     * Gets the {@code SmithyExtension} extension of a {@code Project}.
     *
     * @param project Project to query.
     * @return Returns the extension.
     */
    public static SmithyExtension getSmithyExtension(Project project) {
        return project.getExtensions().getByType(SmithyExtension.class);
    }

    /**
     * Gets the path to a projection plugins output.
     *
     * @param smithyOutputDirectory output directory to get plugin path from
     * @param projection Projection name.
     * @param plugin Plugin name.
     * @return Returns the resolved path.
     */
    public static Path getProjectionPluginPath(File smithyOutputDirectory, String projection, String plugin) {
        if (!smithyOutputDirectory.isDirectory()) {
            throw new RuntimeException("Expected directory for outputDir but found file");
        }
        return smithyOutputDirectory.toPath()
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
    private static Configuration getSmithyCliClasspath(Project project) {
        return getClasspath(project, "smithyCli");
    }

    /**
     * Gets the classpath of a project by name.
     *
     * @param project Project to inspect.
     * @param configurationName Name of the classpath to retrieve.
     * @return Returns the classpath.
     */
    public static Configuration getClasspath(Project project, String configurationName) {
        return project.getConfigurations().getByName(configurationName);
    }

    // TODO: should this be in the jar staging task?
    /**
     * Gets the path to the temp directory where Smithy model resources are placed
     * in the generated JAR of a project.
     *
     * The name of the task is used so that multiple staging directories can be
     * created without conflicts. The task name must uniquely identify the task
     * within a project so there is no concern of task naming collision.
     *
     * @param taskName name of the task using this Temp directory
     * @param outputDir output directory to inspect.
     * @return Returns the classpath.
     */
    public static File getSmithyResourceTempDir(String taskName, File outputDir) {
        return outputDir.toPath()
                .resolve("tmp")
                .resolve("staging-" + taskName)
                .resolve("META-INF")
                .resolve("smithy")
                .toFile();
    }

    /**
     * Gets the provider for the default output directory of projections.
     *
     * @param project Project to inspect.
     * @return Returns the default output directory.
     */
    public static Provider<Directory> getProjectionOutputDirProperty(Project project) {
        return project.getLayout()
            .getBuildDirectory()
            .dir(SMITHY_PROJECTIONS + File.separator + project.getName());
    }

    /**
     * Returns the output directory for Smithy artifacts. The default location will be used unless overridden through
     * the {@link SmithyExtension}.
     *
     * @param project The project to query.
     * @return Returns the resolved directory.
     */
    public static Provider<Directory> outputDirectory(Project project) {
        SmithyExtension extension = getSmithyExtension(project);
        ProjectLayout layout = project.getLayout();

        Provider<Directory> defaultLocation = getProjectionOutputDirProperty(project);
        Provider<Directory> override = layout.dir(extension.getOutputDirectory());
        return override.orElse(defaultLocation);
    }

    /**
     * Executes the Smithy CLI in a separate thread or process.
     *
     * @param executor WorkerExecutor to use for executing CLI command.
     * @param arguments CLI arguments.
     * @param cliClasspath Classpath to use when running the CLI.
     * @param fork whether to fork a new process or not
     */
    public static void executeCli(
            WorkerExecutor executor,
            List<String> arguments,
            FileCollection cliClasspath,
            boolean fork
    ) {
        CliDependencyResolver.validateCliClasspath(cliClasspath);
        Action<? super ClassLoaderWorkerSpec> config = spec -> spec.getClasspath().setFrom(cliClasspath);
        WorkQueue queue = fork ? executor.processIsolation(config) : executor.classLoaderIsolation(config);

        queue.submit(RunCli.class, params -> {
            params.getArguments().set(arguments);

            // The isolated classloader WorkQueue doesn't seem to be as isolated as we need
            // for running the Smithy CLI. Relying on it rather than creating a custom
            // URLClassLoader causes the classpath to seem to inherit cached JARs from places
            // like ~/.gradle/caches/jars-9.
            params.getClassPath().setFrom(cliClasspath);
        });

        queue.await();
    }

    @SmithyInternalApi
    public abstract static class CliConfig implements WorkParameters {
        abstract ListProperty<String> getArguments();

        abstract ConfigurableFileCollection getClassPath();
    }

    @SmithyInternalApi
    public abstract static class RunCli implements WorkAction<CliConfig> {
        @Override
        public void execute() {
            withClassloader(getParameters().getClassPath().getFiles(), classLoader -> {
                withCacheBuster(() -> {
                    try {
                        Class<?> smithyCliClass = classLoader.loadClass("software.amazon.smithy.cli.SmithyCli");
                        Object cli = smithyCliClass.getDeclaredMethod("create").invoke(null);
                        smithyCliClass.getDeclaredMethod("run", List.class)
                                .invoke(cli, getParameters().getArguments().get());
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        }
    }

    private static void withClassloader(Set<File> files, Consumer<ClassLoader> consumer) {
        URL[] paths = new URL[files.size()];
        int i = 0;

        for (File file : files) {
            try {
                paths[i++] = file.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        try (URLClassLoader classLoader = new URLClassLoader(paths)) {
            consumer.accept(classLoader);
        } catch (IOException e) {
            throw unwrapException(e);
        }
    }

    private static void withCacheBuster(Runnable runnable) {
        // URL caching must be disabled when running the Smithy CLI using a
        // custom class loader. An "empty" resource was added to the Gradle
        // plugin to provide access to the global URL-wide caching behavior
        // provided by URLConnection#setDefaultUseCaches. Setting that to
        // false on any URLConnection disables caching on all URLConnections.
        // Not doing this will lead to consistent errors like
        // java.util.zip.ZipException: ZipFile invalid LOC header (bad signature)
        // The default caching setting is restored after invoking the CLI.
        URLConnection cacheBuster = null;
        boolean isCachingEnabled = false;

        try {
            cacheBuster = SmithyUtils.class.getResource("empty").openConnection();
            isCachingEnabled = cacheBuster.getDefaultUseCaches();
            cacheBuster.setDefaultUseCaches(false);
            runnable.run();
        } catch (IOException e) {
            throw new SmithyBuildException(e);
        } finally {
            if (cacheBuster != null) {
                cacheBuster.setDefaultUseCaches(isCachingEnabled);
            }
        }
    }

    private static RuntimeException unwrapException(Throwable current) {
        while (current.getCause() != null) {
            current = current.getCause();
        }
        if (current instanceof RuntimeException) {
            return (RuntimeException) current;
        } else {
            return new GradleException(current.getMessage(), current);
        }
    }

    static String getRelativeSourceSetName(String sourceSetName, String name) {
        return sourceSetName.equals(SourceSet.MAIN_SOURCE_SET_NAME)
                ? name : sourceSetName + StringUtils.capitalize(name);
    }
}
