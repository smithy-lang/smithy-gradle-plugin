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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
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
        if (!Files.isDirectory(dir)) {
            return;
        }

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
            deleteDir(dest.toPath());
            copyDirectory(source.toPath(), dest.toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void withCopy(String projectName, CopyConsumer consumer) {
        File buildDir = createTempDir(projectName).toFile();
        try {
            copyProject(projectName, buildDir);
            consumer.accept(buildDir);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            deleteTempDir(buildDir);
        }
    }

    @FunctionalInterface
    interface CopyConsumer {
        void accept(File file) throws IOException;
    }

    public static Path getProjectDir(String name) {
        return Paths.get(".").resolve("examples").resolve(name);
    }

    public static void assertSmithyBuildRan(BuildResult result) {
        Assertions.assertTrue(result.task(":smithyBuildJar").getOutcome() == TaskOutcome.SUCCESS);
    }

    public static void assertSmithyBuildDidNotRun(BuildResult result) {
        BuildTask task = result.task(":smithyBuildJar");
        Assertions.assertTrue(task == null || task.getOutcome() == TaskOutcome.SKIPPED);
    }

    public static void assertValidationRan(BuildResult result) {
        // e.g., Validated 120 shapes
        Assertions.assertTrue(result.getOutput().contains("shapes"));
    }

    public static void assertArtifactsCreated(File projectDir, String... paths) {
        Path base = projectDir.toPath();
        for (String file : paths) {
            Path dirPath = base.resolve(file);
            Assertions.assertTrue(Files.exists(dirPath), () -> file
                    + " does not exist. The following files exist:\n"
                    + listProjectFiles(base).map(Path::toString).collect(Collectors.joining("\n")));
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

    private static void deleteDir(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }

        Files.walk(dir).map(Path::toFile).sorted((o1, o2) -> -o1.compareTo(o2)).forEach(File::delete);
    }

    private static Stream<Path> listProjectFiles(Path dir) {
        try {
            return Files.find(dir, 999, (p, bfa) -> bfa.isRegularFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void copyDirectory(Path from, Path to) throws IOException {
        Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                // Don't copy the build dirs or private files.
                if (dir.toString().endsWith("build") || dir.toString().endsWith(".gradle")
                        || dir.getFileName().startsWith(".")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                // Create parent directories if they don't exist.
                to.resolve(from.relativize(dir)).toFile().mkdirs();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, to.resolve(from.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
