/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.internal;

import javax.inject.Inject;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;

public class ProjectionArtifactDirectoryContainer extends SmithyArtifactDirectoryContainer {
    private final NamedDomainObjectContainer<SmithyArtifactDirectoryContainer> plugins;

    @Inject
    public ProjectionArtifactDirectoryContainer(String name, final ObjectFactory objectFactory) {
        super(name, objectFactory);
        this.plugins = objectFactory.domainObjectContainer(SmithyArtifactDirectoryContainer.class,
                pluginName -> objectFactory.newInstance(SmithyArtifactDirectoryContainer.class, pluginName));
    }

    public NamedDomainObjectContainer<SmithyArtifactDirectoryContainer> getPlugins() {
        return plugins;
    }
}
