/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.internal;

import javax.inject.Inject;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.internal.file.DefaultSourceDirectorySet;
import org.gradle.api.internal.tasks.TaskDependencyFactory;
import software.amazon.smithy.gradle.SmithySourceDirectorySet;
import software.amazon.smithy.utils.SmithyInternalApi;

@SmithyInternalApi
public class DefaultSmithySourceDirectorySet extends DefaultSourceDirectorySet implements SmithySourceDirectorySet {
    @Inject
    public DefaultSmithySourceDirectorySet(SourceDirectorySet sourceSet, TaskDependencyFactory taskDependencyFactory) {
        super(sourceSet, taskDependencyFactory);
    }
}
