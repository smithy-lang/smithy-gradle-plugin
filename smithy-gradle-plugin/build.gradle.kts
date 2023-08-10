
description = "This project integrates Smithy with Gradle. This plugin can build artifacts " +
        "from Smithy models, generate JARs that contain Smithy models found in Java " +
        "projects, and generate JARs that contain filtered *projections* of Smithy " +
        "models."

<<<<<<< HEAD
gradlePlugin {
    plugins {
        create("software.amazon.smithy") {
            id = "software.amazon.smithy"
            displayName = "Smithy Gradle Plugin"
            description = description
            implementationClass = "software.amazon.smithy.gradle.SmithyPlugin"
        }
    }
=======
ext {
    set("id","software.amazon.smithy")
    set("displayName", "Smithy Gradle Plugin")
    set("implementationClass", "software.amazon.smithy.gradle.SmithyPlugin")
>>>>>>> 5573798 (Refactor to allow for multiple plugins in the repo)
}
