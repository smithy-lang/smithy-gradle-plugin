/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Optional;
import org.gradle.internal.impldep.org.eclipse.jgit.annotations.Nullable;
import software.amazon.smithy.model.traits.DynamicTrait;

/**
 * Base class for all Smithy tasks.
 */
abstract class BaseSmithyTask extends DefaultTask {
    private FileCollection models;
    private boolean allowUnknownTraits;

    /**
     * Gets the list of models to build/validate.
     *
     * <p>These models are also considered "sources" when building a JAR
     * for a project. A source model is a model that appears in the
     * {@code META-INF/smithy} directory of a JAR.
     *
     * @return Returns the models to validate.
     */
    @InputFiles
    @Optional
    @Nullable
    public final FileCollection getModels() {
        return models;
    }

    /**
     * Sets the list of models to build/validate.
     *
     * @param models The custom models to validate.
     */
    public final void setModels(FileCollection models) {
        this.models = models;
    }

    /**
     * Gets whether or not unknown traits in the model should be ignored.
     *
     * <p>By default, the build will fail if unknown traits are encountered.
     * This can be set to true to allow unknown traits to pass through the
     * model and be loaded as a {@link DynamicTrait}.
     *
     * @return Returns true if unknown traits are allowed.
     */
    @Input
    @Optional
    public final boolean getAllowUnknownTraits() {
        return allowUnknownTraits;
    }

    /**
     * Sets whether or not unknown traits are ignored.
     *
     * @param allowUnknownTraits Set to true to ignore unknown traits.
     */
    public final void setAllowUnknownTraits(boolean allowUnknownTraits) {
        this.allowUnknownTraits = allowUnknownTraits;
    }
}
