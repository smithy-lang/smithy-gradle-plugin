
description = "This project integrates Smithy with Gradle. This plugin can build artifacts " +
        "from Smithy models, generate JARs that contain Smithy models found in Java " +
        "projects, and generate JARs that contain filtered *projections* of Smithy " +
        "models."

ext {
    set("id","software.amazon.smithy")
    set("displayName", "Smithy Gradle Plugin")
    set("implementationClass", "software.amazon.smithy.gradle.SmithyPlugin")
}
