/*
 * Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.gradle.tasks;

import java.util.Set;
import java.util.TreeSet;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.tasks.Input;
import org.gradle.jvm.tasks.Jar;
import software.amazon.smithy.gradle.SmithyExtension;
import software.amazon.smithy.gradle.SmithyUtils;

/**
 * Adds Smithy-Tags to the JAR built by the {@link Jar} Task.
 * This Task does not define any outputs. So it will always be executed before the Jar Task.
 */
public class SmithyTagsAction implements Action<Task> {

    private Set<String> tags = new TreeSet<>();

    /**
     * Get the tags that are added to the JAR.
     *
     * <p>These tags are placed in the META-INF/MANIFEST.MF attribute named
     * "Smithy-Tags" as a comma separated list. JARs with Smithy-Tags can be
     * queried when building projections so that the Smithy models found in
     * each matching JAR are placed into the projection JAR.
     *
     * @return Returns the Smithy-Tags values that will be added to the created JAR.
     */
    @Input
    public Set<String> getTags() {
        return tags;
    }

    /**
     * Sets the tags that are added that the JAR manifest in "Smithy-Tags".
     *
     * @param tags Smithy-Tags to add to the JAR.
     * @see #getTags()
     */
    public void setTags(Set<String> tags) {
        this.tags.addAll(tags);
    }

    @Override
    public void execute(Task task) {
        writeHeading("Running smithyTags");

        if (!(task instanceof Jar)) {
            throw new GradleException("SmithyTagsAction can only be used with the Jar task type");
        }

        Jar jar = (Jar) task;

        Project project = jar.getProject();

        if (!jar.isEnabled()) {
            jar.getLogger().info("'jar' task is not enabled, so nothing to do in 'smithyTags'");
            return;
        }

        // Configure the task from the extension if things aren't already setup.
        SmithyExtension extension = SmithyUtils.getSmithyExtension(project);
        tags.addAll(extension.getTags());

        // Always add the group, the group + ":" + name, and the group + ":" + name + ":" + version as tags.
        if (!project.getGroup().toString().isEmpty()) {
            tags.add(project.getGroup().toString());
            tags.add(project.getGroup() + ":" + project.getName());
            tags.add(project.getGroup() + ":" + project.getName() + ":" + project.getVersion());
            jar.getLogger().info("Adding built-in Smithy JAR tags: {}", tags);
        }

        jar.getLogger().info("Adding tags to manifest: {}", tags);
        Attributes attributes = jar.getManifest().getAttributes();
        attributes.put("Smithy-Tags", String.join(", ", tags));
    }

    private void writeHeading(String text) {
//        StyledTextOutput output = getServices().get(StyledTextOutputFactory.class)
//                .create("smithy")
//                .style(StyledTextOutput.Style.Header);
//        output.println(text);
    }
}
