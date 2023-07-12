/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.internal;

import java.io.File;
import java.util.Objects;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.file.FileCollection;


public final class CliDependencyResolver {
    private static final String DEPENDENCY_NOTATION = "software.amazon.smithy:smithy-cli:%s";
    private static final String CLI_CONFIGURATION_NAME = "smithyCli";
    private static final String RUNTIME_CLASSPATH_CONFIG = "runtimeClasspath";
    private static final String SMITHY_CLI_DEP_NAME = "smithy-cli";

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
     * @return the version of the CLI in use
     */
    public static String resolve(Project project) {
        Configuration cli = getCliConfiguration(project);

        // Prefer explicitly set dependency first.
        DependencySet existing = cli.getAllDependencies();
        if (existing.stream().anyMatch(d -> isMatchingDependency(d, SMITHY_CLI_DEP_NAME))) {
            project.getLogger().info("(using explicitly configured Smithy CLI)");
            return existing.stream()
                    .filter(d -> isMatchingDependency(d, SMITHY_CLI_DEP_NAME))
                    .findFirst().get().getVersion();
        }

        // Force projects in the main smithy repo to use an explicit smithy cli dependency
        failIfRunningInMainSmithyRepo(project);

        // If no explicit dependency was found, find the CLI version by scanning
        // and set this as a dependency
        String cliVersion = getCliVersion(project, cli);
        project.getDependencies().add(cli.getName(), String.format(DEPENDENCY_NOTATION, cliVersion));

        return cliVersion;
    }


    private static String getCliVersion(Project project, Configuration cliConfiguration) {
        String cliVersion = detectCliVersionInDependencies(cliConfiguration);

        if (cliVersion != null) {
            project.getLogger().info("(detected Smithy CLI version {})", cliVersion);
        } else {
            // Finally, scan the buildScript dependencies for a smithy-model dependency. This
            // should always be found because the Gradle plugin has a dependency on it.
            cliVersion = scanForSmithyCliVersion(project);
        }
        return cliVersion;
    }

    // Check if there's a dependency on smithy-model somewhere, and assume that version.
    public static String detectCliVersionInDependencies(Configuration configuration) {
        return configuration.getAllDependencies().stream()
                .filter(d -> isMatchingDependency(d, "smithy-model"))
                .map(Dependency::getVersion)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static boolean isMatchingDependency(Dependency dependency, String name) {
        return Objects.equals(dependency.getGroup(), "software.amazon.smithy")
                && dependency.getName().equals(name);
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

    public static Configuration getCliConfiguration(Project project) {
        if (project.getConfigurations().findByName(CLI_CONFIGURATION_NAME) != null) {
            return project.getConfigurations().getByName(CLI_CONFIGURATION_NAME);
        } else {
            // NOTE: the requirement of having the runtime classpath config drives the need for
            // the prerequisite plugins
            return project.getConfigurations().create(CLI_CONFIGURATION_NAME)
                    .extendsFrom(project.getConfigurations().getByName(RUNTIME_CLASSPATH_CONFIG));
        }
    }

    // Subprojects in the main Smithy repo must define an explicit smithy-cli dependency.
    // This is mainly because I couldn't figure out how to add a project dependency.
    private static void failIfRunningInMainSmithyRepo(Project project) {
        if (project.getParent() != null) {
            Project parent = project.getParent();
            if (parent.getGroup().equals("software.amazon.smithy")) {
                for (Project subproject : parent.getSubprojects()) {
                    if (subproject.getPath().equals(":smithy-cli")) {
                        throw new GradleException("Detected that this is the main Smithy repo. "
                                + "You need to add an explicit :project dependency on :smithy-cli");
                    }
                }
            }
        }
    }

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
