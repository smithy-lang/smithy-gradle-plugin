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

package software.amazon.smithy.gradle;

import org.gradle.api.file.FileCollection;
import software.amazon.smithy.model.traits.DynamicTrait;

/**
 * Gradle configuration settings for Smithy.
 */
public class SmithyExtension {

    private FileCollection smithyBuildConfigs;
    private String projection = "source";
    private FileCollection classpath;
    private FileCollection modelDiscoveryClasspath;
    private boolean allowUnknownTraits;

    /**
     * Gets the projection name in use by the extension.
     *
     * @return Returns the projection name and defaults to "source".
     */
    public String getProjection() {
        return projection;
    }

    /**
     * Sets the projection name.
     *
     * <p>There must be a corresponding projection definition in the
     * {@code smithy-build.json} file of the project.
     *
     * @param projection Projection to set.
     */
    public void setProjection(String projection) {
        this.projection = projection;
    }

    /**
     * Gets a custom collection of smithy-build.json files to use when
     * building the model.
     *
     * @return Returns the collection of build configurations.
     */
    public FileCollection getSmithyBuildConfigs() {
        return smithyBuildConfigs;
    }

    /**
     * Sets a custom collection of smithy-build.json files to use when
     * building the model.
     *
     * @param smithyBuildConfigs Sets the collection of build configurations.
     */
    public void setSmithyBuildConfigs(FileCollection smithyBuildConfigs) {
        this.smithyBuildConfigs = smithyBuildConfigs;
    }

    /**
     * Gets the classpath used when loading models, traits, validators, etc.
     *
     * @return Returns the nullable classpath in use.
     */
    public FileCollection getClasspath() {
        return classpath;
    }

    /**
     * Sets the classpath to use when loading models, traits, validators, etc.
     *
     * @param classpath Classpath to use.
     */
    public void setClasspath(FileCollection classpath) {
        this.classpath = classpath;
    }

    /**
     * Gets the classpath used for model discovery.
     *
     * @return Returns the nullable classpath in use.
     */
    public FileCollection getModelDiscoveryClasspath() {
        return modelDiscoveryClasspath;
    }

    /**
     * Sets the classpath to use for model discovery.
     *
     * @param modelDiscoveryClasspath Classpath to use for model discovery.
     */
    public void setModelDiscoveryClasspath(FileCollection modelDiscoveryClasspath) {
        this.modelDiscoveryClasspath = modelDiscoveryClasspath;
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
    public boolean getAllowUnknownTraits() {
        return allowUnknownTraits;
    }

    /**
     * Sets whether or not unknown traits are ignored.
     *
     * @param allowUnknownTraits Set to true to ignore unknown traits.
     */
    public void setAllowUnknownTraits(boolean allowUnknownTraits) {
        this.allowUnknownTraits = allowUnknownTraits;
    }
}
