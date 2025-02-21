/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.internal;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPlugin;
import software.amazon.smithy.gradle.SmithyUtils;
import software.amazon.smithy.utils.SmithyInternalApi;


/**
 * Utility class used to resolve the CLI version and associated CLI dependencies.
 */
@SmithyInternalApi
public final class CliDependencyResolver {
    private static final String DEPENDENCY_NOTATION = "software.amazon.smithy:smithy-cli:%s";
    private static final String SMITHY_CLI_DEP_NAME = "smithy-cli";
    private static final Map<String, String> SMITHY_CLI_PROJECT_CONFIGURATION = new HashMap<>();

    static {
        SMITHY_CLI_PROJECT_CONFIGURATION.put("path", ":smithy-cli");
        SMITHY_CLI_PROJECT_CONFIGURATION.put("configuration", "shadow");
    }

    private CliDependencyResolver() {}

    /**
     * Add the CLI to the "smithyCli" dependencies.
     *
     * <p>The Smithy CLI is invoked in various ways to perform a build. This
     * method ensures that the JARs needed to invoke the CLI with a custom
     * classpath include the JARs needed to run the CLI.
     *
     * <p>The plugin will attempt to detect which version of the Smithy
     * CLI to use when performing validation of the generated JAR. If a
     * CLI version is explicitly defined in the "smithyCli" configuration,
     * then that version is used. Otherwise, a CLI version is inferred by
     * scanning the compileClasspath and runtimeClasspath for instances of
     * the "smithy-model" dependency. If found, the version of smithy-model
     * detected is used for the CLI.
     *
     * @param project Project to add dependencies to.
     *
     * @return Returns the resolved CLI version
     */
    public static String resolve(Project project) {
        Configuration cli = SmithyUtils.getCliConfiguration(project);

        // Prefer explicitly set dependency first.
        Optional<Dependency> explicitCliDepOptional = cli.getAllDependencies().stream()
                .filter(d -> SmithyUtils.isMatchingDependency(d, SMITHY_CLI_DEP_NAME))
                .findFirst();
        if (explicitCliDepOptional.isPresent()) {
            project.getLogger().info(String.format("(using explicitly configured Smithy CLI: %s)",
                            explicitCliDepOptional.get().getVersion()));
            return  explicitCliDepOptional.get().getVersion();
        }

        checkIfRunningInMainSmithyRepo(project);

        // If no explicit dependency was found, find the CLI version by scanning and set this as a dependency
        String cliVersion = getCliVersion(project);
        project.getDependencies().add(cli.getName(), String.format(DEPENDENCY_NOTATION, cliVersion));

        return cliVersion;
    }

    private static String getCliVersion(Project project) {
        String cliVersion = detectCliVersionInRuntimeDependencies(project.getConfigurations());
        if (cliVersion != null) {
            project.getLogger().info("(detected Smithy CLI version {})", cliVersion);
        } else {
            // Finally, scan the buildScript dependencies for a smithy-model dependency. This
            // should always be found because the Gradle plugin has a dependency on it.
            cliVersion = scanForSmithyCliVersion(project);
        }
        return cliVersion;
    }

    /**
     * Check if there's a dependency on smithy-model somewhere, and assume that version.
     *
     * @param configurations Configuration contain to use to search for configurations
     *
     * @return version of cli available in configuration
     */
    public static String detectCliVersionInRuntimeDependencies(ConfigurationContainer configurations) {
        // If the runtimeClasspath configuration does not exist we cannot scan runtime deps
        if (configurations.findByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME) == null) {
            return null;
        }
        Configuration runtimeClasspath = configurations.getByName(
                JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
        return runtimeClasspath.getResolvedConfiguration().getResolvedArtifacts().stream()
                .filter(ra -> ra.getName().equals("smithy-model"))
                .map(ra -> ra.getModuleVersion().getId().getVersion())
                .findFirst()
                .orElse(null);
    }

    private static String scanForSmithyCliVersion(Project project) {
        // Finally, scan the buildScript dependencies for a smithy-model dependency. This
        // should be found because the Gradle plugin has a dependency on it.
        for (File jar : project.getBuildscript().getConfigurations().getByName("classpath")) {
            String name = jar.toString();
            int smithyCliPosition = name.lastIndexOf("smithy-cli-");
            if (smithyCliPosition > -1 && name.endsWith(".jar")) {
                String cliVersion = name.substring(
                        smithyCliPosition + "smithy-cli-".length(), name.length() - ".jar".length());
                project.getLogger().info("(scanned and found a Smithy CLI version {}. "
                        + "You will need to add an explicit dependency on smithy-model "
                        + "if publishing a JAR)", cliVersion);
                return cliVersion;
            }
        }

        // This should never happen since the smithy plugin has a dependency on smithy-cli.
        throw new GradleException("Unable to determine a smithy-cli dependency version. Please add an "
                + "explicit dependency on smithy-model.");
    }

    // Check if plugin is being run in the main smithy repo, and, if so, intrinsically depend on the CLI
    private static void checkIfRunningInMainSmithyRepo(Project project) {
        if (project.getParent() != null) {
            Project parent = project.getParent();
            if (parent.getGroup().equals("software.amazon.smithy")) {
                for (Project subproject : parent.getSubprojects()) {
                    if (subproject.getPath().equals(":smithy-cli")) {
                        // Add a dependency on the smithy-cli, shadow configuration
                        project.getDependencies().project(SMITHY_CLI_PROJECT_CONFIGURATION);
                        project.getLogger().info("Detected that this is the main Smithy repo. "
                                + "Adding dependency on (:smithy-cli - shadow)");
                    }
                }
            }
        }
    }

    /**
     * Checks that the provided classpath contains the smithy cli.
     *
     * @param cliClasspath classpath to validate
     */
    public static void validateCliClasspath(FileCollection cliClasspath) {
        if (!cliClasspath.getAsPath().contains("smithy-cli")) {
            throw new GradleException("Could not find `smithy-cli` in the CLI classpath");
        }

        for (File file : cliClasspath) {
            if (!file.exists()) {
                throw new GradleException("CLI classpath JAR does not exist: " + file);
            }
        }
    }
}
