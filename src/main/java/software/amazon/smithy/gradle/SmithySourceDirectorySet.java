/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import org.gradle.api.file.SourceDirectorySet;

/**
 * A {@code SmithySourceDirectorySet} defines the properties and methods added to a
 * {@link org.gradle.api.tasks.SourceSet} by the {@link SmithyPlugin}.
 */
public interface SmithySourceDirectorySet extends SourceDirectorySet {
    /**
     * Name of the source set extension contributed by the Smithy plugin.
     */
    String NAME = "smithy";
}
