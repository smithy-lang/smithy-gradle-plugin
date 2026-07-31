/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package software.amazon.smithy.gradle.examples;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import software.amazon.smithy.build.PluginContext;
import software.amazon.smithy.build.SmithyBuildPlugin;
import software.amazon.smithy.gradle.examples.spi.ServiceLoaderProbe;

public final class ServiceLoaderProbePlugin implements SmithyBuildPlugin {
    @Override
    public String getName() {
        return "service-loader-probe";
    }

    @Override
    public void execute(PluginContext context) {
        List<ServiceLoaderProbe> probes = new ArrayList<>();
        ServiceLoader.load(ServiceLoaderProbe.class).forEach(probes::add);

        if (probes.size() != 1) {
            throw new IllegalStateException("Expected exactly one ServiceLoaderProbe provider but found "
                    + probes.size());
        }

        probes.get(0).run();
    }
}
