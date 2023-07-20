/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.internal;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.gradle.api.Named;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.model.ObjectFactory;


public class SmithyArtifactDirectoryContainer implements Named {
    private final String name;
    private final DirectoryProperty directory;

    @Inject
    public SmithyArtifactDirectoryContainer(String name, ObjectFactory objectFactory) {
        this.name = Objects.requireNonNull(name);
        this.directory = objectFactory.directoryProperty();
    }

    @Override
    public @Nonnull String getName() {
        return name;
    }

    public DirectoryProperty getDirectory() {
        return directory;
    }
}
