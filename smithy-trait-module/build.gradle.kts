description = "Configures a Java library package for Smithy traits, using " +
        "Smithy's trait-codegen plugin to generate Java implementation of traits."

plugins {
    id("smithy-gradle-plugin.plugin-conventions")
}

gradlePlugin {
    plugins {
        create("smithy-trait-module-plugin") {
            id = "${group}.smithy-trait-module"
            displayName = "Smithy Gradle Trait Package plugin."
            description = project.description
            implementationClass = "software.amazon.smithy.gradle.SmithyTraitModulePlugin"
            tags.addAll("smithy", "api", "building")
        }
    }
}

dependencies {
    implementation(project(":smithy-jar"))
    implementation(project(":smithy-base"))
}
