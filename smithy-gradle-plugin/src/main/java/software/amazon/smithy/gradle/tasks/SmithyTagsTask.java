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
import org.gradle.api.DefaultTask;
import org.gradle.api.java.archives.Attributes;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.internal.logging.text.StyledTextOutput;
import org.gradle.internal.logging.text.StyledTextOutputFactory;
import org.gradle.jvm.tasks.Jar;

/**
 * Adds Smithy-Tags to the JAR built by the {@link Jar} Task.
 * This Task does not define any outputs. So it will always be executed before the Jar Task.
 */
public class SmithyTagsTask extends DefaultTask {

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

    @TaskAction
    public void execute() {
        writeHeading("Running smithyTags");

        if (!getProject().getTasks().getByName("jar").getEnabled()) {
            getLogger().info("'jar' task is not enabled, so nothing to do in 'smithyTags'");
            return;
        }

        // Always add the group, the group + ":" + name, and the group + ":" + name + ":" + version as tags.
        if (!getProject().getGroup().toString().isEmpty()) {
            tags.add(getProject().getGroup().toString());
            tags.add(getProject().getGroup() + ":" + getProject().getName());
            tags.add(getProject().getGroup() + ":" + getProject().getName() + ":" + getProject().getVersion());
            getLogger().info("Adding built-in Smithy JAR tags: {}", tags);
        }

        getProject().getTasks().withType(Jar.class, task -> {
            getLogger().info("Adding tags to manifest: {}", tags);
            Attributes attributes = task.getManifest().getAttributes();
            attributes.put("Smithy-Tags", String.join(", ", tags));
        });
    }

    private void writeHeading(String text) {
        StyledTextOutput output = getServices().get(StyledTextOutputFactory.class)
                .create("smithy")
                .style(StyledTextOutput.Style.Header);
        output.println(text);
    }
}
