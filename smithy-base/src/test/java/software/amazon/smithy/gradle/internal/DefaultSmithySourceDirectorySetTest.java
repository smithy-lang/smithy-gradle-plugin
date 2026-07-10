/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.File;
import java.util.stream.Collectors;
import org.gradle.api.Project;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.gradle.SmithySourceDirectorySet;

public class DefaultSmithySourceDirectorySetTest {
    private Project project;
    private ObjectFactory objectFactory;
    private SourceDirectorySet delegate;
    private SmithySourceDirectorySet sds;

    @BeforeEach
    public void init() {
        project = ProjectBuilder.builder().build();
        objectFactory = project.getObjects();
        delegate = objectFactory.sourceDirectorySet("smithy", "Smithy sources");
        sds = objectFactory.newInstance(DefaultSmithySourceDirectorySet.class, delegate);
    }

    @Test
    public void forwardsNameToDelegate() {
        assertThat(sds.getName(), equalTo(delegate.getName()));
        assertThat(sds.getName(), equalTo("smithy"));
    }

    @Test
    public void fluentMethodsReturnTheWrapperNotTheDelegate() {
        // Callers chaining on the source set must keep the SmithySourceDirectorySet type, so the
        // fluent mutators must return the wrapper itself rather than leaking the delegate.
        assertSame(sds, sds.srcDir("model"));
        assertSame(sds, sds.srcDirs("a", "b"));
        assertSame(sds, sds.include("**/*.smithy"));
        assertSame(sds, sds.exclude("**/*.txt"));
    }

    @Test
    public void mutationsThroughTheWrapperReachTheDelegate() {
        sds.srcDir("model");
        sds.include("**/*.smithy");

        // The delegate is the source of truth, so state set via the wrapper must be visible on it.
        assertThat(delegate.getSrcDirs(), hasItem(project.file("model")));
        assertThat(delegate.getIncludes(), contains("**/*.smithy"));

        // ...and the wrapper must report the same state back.
        assertThat(sds.getSrcDirs(), equalTo(delegate.getSrcDirs()));
        assertThat(sds.getIncludes(), contains("**/*.smithy"));
    }

    @Test
    public void readsAreBackedByTheDelegate() {
        delegate.srcDir("other");

        // A read on the wrapper reflects state set directly on the delegate.
        assertThat(sds.getSrcDirs().stream().map(File::getName).collect(Collectors.toList()),
                hasItem("other"));
    }
}
