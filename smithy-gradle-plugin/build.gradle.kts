
description = "This project integrates Smithy with Gradle. This plugin can build artifacts " +
        "from Smithy models, generate JARs that contain Smithy models found in Java " +
        "projects, and generate JARs that contain filtered *projections* of Smithy " +
        "models."

gradlePlugin {
    website.set("https://github.com/smithy-lang/smithy")
    vcsUrl.set("https://github.com/smithy-lang/smithy")
    plugins {
        create("software.amazon.smithy") {
            id = "software.amazon.smithy"
            displayName = "Smithy Gradle Plugin"
            description = description
            implementationClass = "software.amazon.smithy.gradle.SmithyPlugin"
            tags.addAll("smithy", "api", "building")
        }
    }
}
