/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.internal;

import groovy.lang.Closure;
import java.io.File;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.DirectoryTree;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.util.PatternFilterable;
import software.amazon.smithy.gradle.SmithySourceDirectorySet;
import software.amazon.smithy.utils.SmithyInternalApi;


/**
 * Default implementation of {@link SmithySourceDirectorySet}.
 *
 * <p>This decorates a standard {@link SourceDirectorySet} obtained from
 * {@link org.gradle.api.model.ObjectFactory#sourceDirectorySet} and forwards every method to it,
 * rather than extending Gradle's internal {@code DefaultSourceDirectorySet}. Delegating through the
 * public {@link SourceDirectorySet} interface keeps the plugin insulated from changes to Gradle's
 * internal source set implementation, which is not a supported API and has changed between Gradle
 * major versions.
 */
@SmithyInternalApi
public class DefaultSmithySourceDirectorySet implements SmithySourceDirectorySet {
    private final SourceDirectorySet delegate;

    @Inject
    public DefaultSmithySourceDirectorySet(SourceDirectorySet delegate) {
        this.delegate = delegate;
    }

    @Override
    public SmithySourceDirectorySet srcDir(Object srcDir) {
        delegate.srcDir(srcDir);
        return this;
    }

    @Override
    public SmithySourceDirectorySet srcDirs(Object... srcDirs) {
        delegate.srcDirs(srcDirs);
        return this;
    }

    @Override
    public SmithySourceDirectorySet setSrcDirs(Iterable<?> srcDirs) {
        delegate.setSrcDirs(srcDirs);
        return this;
    }

    @Override
    public SmithySourceDirectorySet source(SourceDirectorySet source) {
        delegate.source(source);
        return this;
    }

    @Override
    public Set<File> getSrcDirs() {
        return delegate.getSrcDirs();
    }

    @Override
    public FileCollection getSourceDirectories() {
        return delegate.getSourceDirectories();
    }

    @Override
    public Set<DirectoryTree> getSrcDirTrees() {
        return delegate.getSrcDirTrees();
    }

    @Override
    public PatternFilterable getFilter() {
        return delegate.getFilter();
    }

    @Override
    public DirectoryProperty getDestinationDirectory() {
        return delegate.getDestinationDirectory();
    }

    @Override
    public Provider<Directory> getClassesDirectory() {
        return delegate.getClassesDirectory();
    }

    @Override
    public <T extends Task> void compiledBy(TaskProvider<T> taskProvider, Function<T, DirectoryProperty> mapping) {
        delegate.compiledBy(taskProvider, mapping);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }

    @Override
    public Set<String> getIncludes() {
        return delegate.getIncludes();
    }

    @Override
    public Set<String> getExcludes() {
        return delegate.getExcludes();
    }

    @Override
    public SmithySourceDirectorySet setIncludes(Iterable<String> includes) {
        delegate.setIncludes(includes);
        return this;
    }

    @Override
    public SmithySourceDirectorySet setExcludes(Iterable<String> excludes) {
        delegate.setExcludes(excludes);
        return this;
    }

    @Override
    public SmithySourceDirectorySet include(String... includes) {
        delegate.include(includes);
        return this;
    }

    @Override
    public SmithySourceDirectorySet include(Iterable<String> includes) {
        delegate.include(includes);
        return this;
    }

    @Override
    public SmithySourceDirectorySet include(Spec<FileTreeElement> includeSpec) {
        delegate.include(includeSpec);
        return this;
    }

    @Override
    public SmithySourceDirectorySet include(Closure includeSpec) {
        delegate.include(includeSpec);
        return this;
    }

    @Override
    public SmithySourceDirectorySet exclude(String... excludes) {
        delegate.exclude(excludes);
        return this;
    }

    @Override
    public SmithySourceDirectorySet exclude(Iterable<String> excludes) {
        delegate.exclude(excludes);
        return this;
    }

    @Override
    public SmithySourceDirectorySet exclude(Spec<FileTreeElement> excludeSpec) {
        delegate.exclude(excludeSpec);
        return this;
    }

    @Override
    public SmithySourceDirectorySet exclude(Closure excludeSpec) {
        delegate.exclude(excludeSpec);
        return this;
    }

    @Override
    public FileTree matching(Closure filterConfigClosure) {
        return delegate.matching(filterConfigClosure);
    }

    @Override
    public FileTree matching(Action<? super PatternFilterable> filterConfigAction) {
        return delegate.matching(filterConfigAction);
    }

    @Override
    public FileTree matching(PatternFilterable patterns) {
        return delegate.matching(patterns);
    }

    @Override
    public FileTree visit(FileVisitor visitor) {
        return delegate.visit(visitor);
    }

    @Override
    public FileTree visit(Closure visitor) {
        return delegate.visit(visitor);
    }

    @Override
    public FileTree visit(Action<? super FileVisitDetails> visitor) {
        return delegate.visit(visitor);
    }

    @Override
    public FileTree plus(FileTree fileTree) {
        return delegate.plus(fileTree);
    }

    @Override
    public FileTree getAsFileTree() {
        return delegate.getAsFileTree();
    }

    @Override
    public File getSingleFile() {
        return delegate.getSingleFile();
    }

    @Override
    public Set<File> getFiles() {
        return delegate.getFiles();
    }

    @Override
    public boolean contains(File file) {
        return delegate.contains(file);
    }

    @Override
    public String getAsPath() {
        return delegate.getAsPath();
    }

    @Override
    public FileCollection plus(FileCollection collection) {
        return delegate.plus(collection);
    }

    @Override
    public FileCollection minus(FileCollection collection) {
        return delegate.minus(collection);
    }

    @Override
    public FileCollection filter(Closure filterClosure) {
        return delegate.filter(filterClosure);
    }

    @Override
    public FileCollection filter(Spec<? super File> filterSpec) {
        return delegate.filter(filterSpec);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public Provider<Set<FileSystemLocation>> getElements() {
        return delegate.getElements();
    }

    @Override
    public void addToAntBuilder(Object builder, String nodeName, FileCollection.AntType type) {
        delegate.addToAntBuilder(builder, nodeName, type);
    }

    @Override
    public Object addToAntBuilder(Object builder, String nodeName) {
        return delegate.addToAntBuilder(builder, nodeName);
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return delegate.getBuildDependencies();
    }

    @Override
    public Iterator<File> iterator() {
        return delegate.iterator();
    }
}
