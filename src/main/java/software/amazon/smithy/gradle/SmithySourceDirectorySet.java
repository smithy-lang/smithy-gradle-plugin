/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import java.util.List;
import org.gradle.api.file.SourceDirectorySet;
import software.amazon.smithy.utils.ListUtils;

/**
 * A {@code SmithySourceDirectorySet} defines the properties and methods added to a
 * {@link org.gradle.api.tasks.SourceSet} by the {@link SmithyPlugin}.
 */
public interface SmithySourceDirectorySet extends SourceDirectorySet {
    /**
     * Name of the source set extension contributed by the Smithy plugin.
     */
    String NAME = "smithy";

    /**
     * Source Directories this Source set will search for Smithy model files in.
     */
    List<String> SOURCE_DIRS = ListUtils.of(
            "model", "src/$name/smithy", "src/$name/resources/META-INF/smithy");
}
