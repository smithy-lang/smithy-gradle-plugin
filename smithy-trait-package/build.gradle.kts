description = "Creates a ."

plugins {
    id("smithy-gradle-plugin.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("smithy-trait-package-plugin") {
            id = "${group}.smithy-trait-package"
            displayName = "Smithy Gradle Trait Package plugin."
            description = project.description
            implementationClass = "software.amazon.smithy.gradle.SmithyTraitPackagePlugin"
            tags.addAll("smithy", "api", "building")
        }
    }
}

dependencies {
    implementation(project(":smithy-jar"))
    implementation(project(":smithy-base"))
}
