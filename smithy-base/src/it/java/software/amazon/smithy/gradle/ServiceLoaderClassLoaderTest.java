/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Regression test for default ServiceLoader usage inside code loaded by the Smithy CLI.
 *
 * <p>The build plugin calls ServiceLoader.load(...) without an explicit class loader,
 * so it depends on the Gradle plugin setting the thread context class loader to the
 * same loader that loaded Smithy.
 */
public class ServiceLoaderClassLoaderTest {
    @Test
    public void buildPluginCanUseDefaultServiceLoader() {
        Utils.withCopy("base-plugin/service-loader-classloading", buildDir -> {
            BuildResult result = Utils.createGradleRunner()
                    .forwardOutput()
                    .withProjectDir(buildDir)
                    .withArguments("clean", "build", "--stacktrace")
                    .build();

            Utils.assertSmithyBuildTaskRan(result);
            Utils.assertArtifactsCreated(buildDir,
                    "build/smithyprojections/service-loader-classloading/source/build-info/smithy-build-info.json",
                    "build/smithyprojections/service-loader-classloading/source/model/model.json",
                    "build/smithyprojections/service-loader-classloading/source/sources/main.smithy",
                    "build/smithyprojections/service-loader-classloading/source/sources/manifest");

            Assertions.assertFalse(
                    result.getOutput().contains("not a subtype"),
                    "Unexpected ServiceLoader class loading failure in output:\n" + result.getOutput());
            Assertions.assertFalse(
                    result.getOutput().contains("ServiceConfigurationError"),
                    "Unexpected ServiceLoader class loading failure in output:\n" + result.getOutput());
        });
    }
}
