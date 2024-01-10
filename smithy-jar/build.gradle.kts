description = "Adds built Smithy files to an existing jar task such as that created " +
        "by the Java or Kotlin plugins. The smithy-jar plugin also adds build metadata " +
        "and tags to the JAR's MANIFEST. The smithy-jar plugin applies the smithy-base " +
        "plugin when it is applied."

gradlePlugin {
    plugins {
        create("smithy-jar-plugin") {
            id = "${group}.smithy-jar"
            displayName = "Smithy Gradle Jar Packaging Plugin"
            description = project.description
            implementationClass = "software.amazon.smithy.gradle.SmithyJarPlugin"
            tags.addAll("smithy", "api", "building")
        }
    }
}

dependencies {
    implementation(project(":smithy-base"))
}