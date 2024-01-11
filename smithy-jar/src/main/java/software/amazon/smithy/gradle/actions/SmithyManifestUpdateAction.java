/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle.actions;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.java.archives.Attributes;
import org.gradle.jvm.tasks.Jar;
import org.gradle.util.GradleVersion;
import software.amazon.smithy.gradle.SmithyGradleVersion;


/**
 * Action that updates a JAR's manifest with Smithy-specific attributes.
 *
 * <p>This action adds projection tags to JAR manifest via the {@code SmithyTags} property.
 * A number of headers are also included in the manifest to add basic build info such as
 * JDK version used for the build and the build timestamp.
 */
public final class SmithyManifestUpdateAction implements Action<Task> {
    private static final String BUILD_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private final Set<String> tags = new HashSet<>();

    public SmithyManifestUpdateAction(Project project, Set<String> tags) {
        addDefaultTags(project);
        this.tags.addAll(tags);
    }

    @Override
    public void execute(@Nonnull Task task) {
        // Note: this is necessary because "doFirst" only allows actions with Task inputs
        if (!(task instanceof Jar)) {
            throw new GradleException("SmithyManifestUpdateAction expected task of type Jar but found "
                    + task.getClass());
        }
        Jar jar = (Jar) task;
        Attributes attributes = jar.getManifest().getAttributes();
        addBuildInfo(attributes);
        addTagsToManifest(attributes);
    }

    private void addBuildInfo(Attributes manifestAttributes) {
        manifestAttributes.put("Build-Timestamp",
                new java.text.SimpleDateFormat(BUILD_TIMESTAMP_FORMAT).format(new Date()));
        manifestAttributes.put("Created-With", "Smithy-Gradle-Plugin (" + SmithyGradleVersion.VERSION
                + "), Gradle (" + GradleVersion.current().getVersion() + ")");
        manifestAttributes.put("Build-Jdk", System.getProperty("java.version"));
        manifestAttributes.put("Build-OS", System.getProperty("os.name")
                + " " + System.getProperty("os.arch") + " "
                + System.getProperty("os.version"));
    }

    private void addDefaultTags(Project project) {
        // Always add the group, the group + ":" + name, and the group + ":" + name + ":" + version as tags.
        if (!project.getGroup().toString().isEmpty()) {
            tags.add(project.getGroup().toString());
            tags.add(project.getGroup() + ":" + project.getName());
            tags.add(project.getGroup() + ":" + project.getName() + ":" + project.getVersion());
            project.getLogger().info("Adding built-in Smithy JAR tags: {}", tags);
        }
    }

    private void addTagsToManifest(Attributes manifestAttributes) {
        manifestAttributes.put("Smithy-Tags", String.join(", ", tags));
    }
}
