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

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class AddsTagsTest {
    @Test
    public void addsSmithyTagsToJars() {
        Utils.withCopy("adds-tags", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);
            Utils.assertJarContains(buildDir, "build/libs/adds-tags-9.9.9.jar",
                    "META-INF/MANIFEST.MF",
                    "META-INF/smithy/manifest",
                    "META-INF/smithy/main.smithy");

            JarFile jar = new JarFile(new File(buildDir, "build/libs/adds-tags-9.9.9.jar"));
            Manifest manifest = jar.getManifest();
            String tags = (String) manifest.getMainAttributes().get(new Attributes.Name("Smithy-Tags"));
            jar.close();
            String[] tagValues = tags.split(", ");

            assertThat(Arrays.asList(tagValues), containsInAnyOrder(
                    "software.amazon.smithy", "software.amazon.smithy:adds-tags",
                    "software.amazon.smithy:adds-tags:9.9.9", "Foo", "Baz"));
        });
    }
}
