import org.apache.tools.ant.filters.ReplaceTokens

description = ""

gradlePlugin {
    plugins {
        create("smithy-jar-plugin") {
            id = "software.amazon.smithy.gradle.smithy-jar"
            displayName = "Smithy Gradle Jar Packaging Plugin"
            description = project.description
            implementationClass = "software.amazon.smithy.gradle.SmithyJarPlugin"
            tags.addAll("smithy", "api", "building")
        }
    }
}

dependencies {
    implementation(project(":smithy-base-plugin"))
}