import org.apache.tools.ant.filters.ReplaceTokens

description = "This plugin sets up the basic capabilities necessary for building Smithy models." +
        "Applying this plugin will create the basic source sets and configurations needed for smithy" +
        "projects. It will also create a smithy build task that will build and validate all the " +
        "smithy models in the project."

gradlePlugin {
    website.set("https://github.com/smithy-lang/smithy")
    vcsUrl.set("https://github.com/smithy-lang/smithy")
    plugins {
        create("smithy-base-plugin") {
            id = "smithy-base"
            displayName = "Smithy Gradle Base Plugin"
            description = description
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