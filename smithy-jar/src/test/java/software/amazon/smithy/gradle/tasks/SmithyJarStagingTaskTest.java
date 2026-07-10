/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SmithyJarStagingTaskTest {
    private Project testProject;

    @BeforeEach
    public void init() {
        testProject = ProjectBuilder.builder().build();
    }

    @Test
    public void validateDefaults() {
        SmithyJarStagingTask stagingTask = testProject.getTasks()
                .create("smithyUnitTestJarStaging",
                        SmithyJarStagingTask.class);

        assertEquals(stagingTask.getProjection().get(), "source");
        assertEquals(stagingTask.getOutputDir().get(), testProject.getLayout().getBuildDirectory().get());
    }
}
