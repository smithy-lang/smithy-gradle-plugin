/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class SmithyGradleVersionTest {
    private static final String TEST_OVERRIDE_VERSION = "0.0.Alpha-Test";

    @Test
    public void versionOverrideWorks() {
        assertEquals(SmithyGradleVersion.VERSION, TEST_OVERRIDE_VERSION);
    }
}
