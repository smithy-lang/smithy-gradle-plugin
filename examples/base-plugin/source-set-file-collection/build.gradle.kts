// This example consumes the "smithy" source set directly as a file collection, which is an
// integration test that the source set can be used anywhere Gradle accepts a FileCollection
// (e.g. `from(...)`). The source set is a plugin-provided implementation of SourceDirectorySet,
// so this exercises that it interoperates with Gradle's file APIs across supported versions.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-base").version("1.4.0")
}

val copySmithySources = tasks.register<Copy>("copySmithySources") {
    from(sourceSets.main.get().extensions.getByName("smithy"))
    into(layout.buildDirectory.dir("collected-smithy-sources"))
}

tasks["build"].dependsOn(copySmithySources)

repositories {
    mavenLocal()
    mavenCentral()
}
