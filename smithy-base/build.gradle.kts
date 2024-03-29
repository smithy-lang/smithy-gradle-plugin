import org.apache.tools.ant.filters.ReplaceTokens

description = "This plugin sets up the basic capabilities necessary for building Smithy models. " +
        "Applying this plugin will create the basic source sets and configurations needed for Smithy " +
        "projects. It will also create a smithy build task that will build and validate all the " +
        "Smithy models in the project."

plugins {
    id("smithy-gradle-plugin.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("smithy-base-plugin") {
            id = "${group}.smithy-base"
            displayName = "Smithy Gradle Base Plugin"
            description = project.description
            implementationClass = "software.amazon.smithy.gradle.SmithyBasePlugin"
            tags.addAll("smithy", "api", "building")
        }
    }
}

// Update the version.properties file to reflect current version of project
tasks.withType<ProcessResources> {
    include("**/*")
    filter<ReplaceTokens>("tokens" to mapOf("SmithyGradleVersion" to version))
}

// Use Junit5's test runner.
tasks.withType<Test> {
    // Override version in tests
    environment( "smithygradle.version.override", "0.0.Alpha-Test");
}