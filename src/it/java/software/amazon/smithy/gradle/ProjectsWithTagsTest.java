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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.ShapeId;

public class ProjectsWithTagsTest {
    @Test
    public void testProjectionWithSourceTags() {
        Utils.withCopy("projects-with-tags", buildDir -> {
            BuildResult result = GradleRunner.create()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildRan(result);
            Utils.assertValidationRan(result);
            Utils.assertJarContains(buildDir,
                                    "build/libs/projects-with-tags.jar",
                                    "META-INF/smithy/manifest",
                                    "META-INF/smithy/model.json");

            Model model = Model.assembler()
                    .addImport(buildDir.toPath().resolve("build").resolve("libs").resolve("projects-with-tags.jar"))
                    .assemble()
                    .unwrap();

            Assertions.assertTrue(model.getShapeIndex().getShape(ShapeId.from("foo.baz#Integer")).isPresent());
            Assertions.assertTrue(model.getShapeIndex().getShape(ShapeId.from("foo.baz#Float")).isPresent());
            Assertions.assertTrue(model.getShapeIndex().getShape(ShapeId.from("smithy.example#Baz")).isPresent());
        });
    }
}
