import software.amazon.smithy.gradle.tasks.SmithyBuildTask

// This example builds Smithy models using a custom build task
// and disables the default "smithyBuildJar" task. This allows
// for a more granular level of control in when the build runs
// and the classpath used when building.

plugins {
    id("software.amazon.smithy.gradle.smithy-base").version("1.0.0")
}

tasks.create<SmithyBuildTask>("doit") {
    models.set(files("model/"))
    smithyBuildConfigs.set(files("smithy-build.json"))
}

tasks["build"].finalizedBy(tasks["doit"])

repositories {
    mavenLocal()
    mavenCentral()
}
