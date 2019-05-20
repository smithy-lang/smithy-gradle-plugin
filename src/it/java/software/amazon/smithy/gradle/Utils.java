package software.amazon.smithy.gradle;/*
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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Assertions;

public final class Utils {
    private Utils() {}

    public static Path createTempDir(String name) {
        try {
            return Files.createTempDirectory(name.replace("/", "_"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void deleteTempDir(Path dir) {
        try {
            Files.walk(dir).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void deleteTempDir(File dir) {
        deleteTempDir(dir.toPath());
    }

    public static void copyProject(String name, File dest) {
        File source = getProjectDir(name).toFile();

        try {
            FileUtils.deleteQuietly(dest);
            FileUtils.copyDirectory(source, dest, f -> {
                // Don't copy the build dir.
                if (f.toString().endsWith("build") || f.toString().endsWith(".gradle")) {
                    return false;
                }

                // Skip hidden files.
                return !f.getName().startsWith(".");
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void withCopy(String projectName, Consumer<File> consumer) {
        File buildDir = Utils.createTempDir(projectName).toFile();
        try {
            copyProject(projectName, buildDir);
            consumer.accept(buildDir);
        } finally {
            Utils.deleteTempDir(buildDir);
        }
    }

    public static Path getProjectDir(String name) {
        return Paths.get(".").resolve("examples").resolve(name);
    }

    public static void assertSmithyBuildRan(BuildResult result) {
        Assertions.assertTrue(result.task(":smithyBuild").getOutcome() == TaskOutcome.SUCCESS);
    }

    public static void assertValidationRan(BuildResult result) {
        Assertions.assertTrue(result.getOutput().contains("Smithy validation complete"));
    }

    public static void assertValidationDidNotRun(BuildResult result) {
        Assertions.assertFalse(result.getOutput().contains("Smithy validation complete"));
    }

    public static void assertArtifactsCreated(File projectDir, String... paths) {
        Path base = projectDir.toPath();
        for (String file : paths) {
            Assertions.assertTrue(Files.exists(base.resolve(file)), () -> file
                    + " does not exist. The following files exist:\n"
                    + FileUtils.listFiles(projectDir, null, true).stream()
                            .map(File::toString)
                            .collect(Collectors.joining("\n")));
        }
    }

    public static void assertArtifactsNotCreated(File projectDir, String... paths) {
        Path base = projectDir.toPath();
        for (String file : paths) {
            Assertions.assertFalse(Files.exists(base.resolve(file)), () -> file  + " should not exist");
        }
    }

    public static void assertJarContains(File projectDir, String jarPath, String... jarEntries) {
        try (JarFile jar = new JarFile(new File(projectDir, jarPath))) {
            for (String jarEntry : jarEntries) {
                Assertions.assertNotNull(jar.getEntry(jarEntry), "JAR entry `" + jarEntry + "` does not exist in `"
                        + jarPath + "`. This JAR contains the following entries:\n"
                        + Collections.list(jar.entries()).stream()
                                .map(JarEntry::getName)
                                .collect(Collectors.joining("\n")));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
