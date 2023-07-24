/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package software.amazon.smithy.gradle;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.gradle.api.GradleException;

/**
 * Represents a Smithy-Gradle-Plugin version.
 */
public final class SmithyGradleVersion {
    public static final String VERSION_OVERRIDE_VAR = "smithygradle.version.override";
    public static final String VERSION = resolveVersion();
    private static final String VERSION_RESOURCE_NAME = "version.properties";
    private static final String VERSION_NUMBER_PROPERTY = "version";

    private SmithyGradleVersion() {}

    private static String resolveVersion() {
        // Check for resources file with Version info
        try (InputStream inputStream = SmithyGradleVersion.class.getResourceAsStream(VERSION_RESOURCE_NAME)) {
            if (inputStream == null) {
                throw new GradleException(format("Version file '%s' not found.", VERSION_RESOURCE_NAME));
            }
            Properties properties = new Properties();
            properties.load(inputStream);
            String version = properties.get(VERSION_NUMBER_PROPERTY).toString();

            // Allow version to be overridden if needed for tests
            String overrideVersion = System.getenv(VERSION_OVERRIDE_VAR);
            if (overrideVersion != null) {
                return overrideVersion;
            }
            return version;

        } catch (IOException e) {
            throw new GradleException(format("Failed to read Version file %S", VERSION_RESOURCE_NAME), e);
        }
    }
}
