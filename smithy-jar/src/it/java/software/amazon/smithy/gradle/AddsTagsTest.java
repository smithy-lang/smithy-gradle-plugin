/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.io.File;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;

public class AddsTagsTest {
    @Test
    public void addsSmithyTagsToJars() {
        Utils.withCopy("jar-plugin/adds-tags", buildDir -> {
            BuildResult result = Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertValidationRan(result);
            Utils.assertJarContains(buildDir,
                    "build/libs/adds-tags-9.9.9.jar",
                    "META-INF/MANIFEST.MF",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/main.smithy");

            JarFile jar = new JarFile(new File(buildDir, "build/libs/adds-tags-9.9.9.jar"));
            Manifest manifest = jar.getManifest();
            String tags = (String) manifest.getMainAttributes().get(new Attributes.Name("Smithy-Tags"));
            jar.close();
            String[] tagValues = tags.split(", ");

            assertThat(Arrays.asList(tagValues),
                    containsInAnyOrder(
                            "software.amazon.smithy",
                            "software.amazon.smithy:adds-tags",
                            "software.amazon.smithy:adds-tags:9.9.9",
                            "Foo",
                            "Baz"));
        });
    }
}
