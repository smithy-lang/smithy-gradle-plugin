import software.amazon.smithy.gradle.tasks.SmithyBuild

// This example builds Smithy models using a custom build task
// and disables the default "smithyBuildJar" task. This allows
// for a more granular level of control in when the build runs
// and the classpath used when building.

plugins {
    id("software.amazon.smithy").version("0.5.3")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
}

tasks["jar"].enabled = false
tasks["smithyBuildJar"].enabled = false

tasks.create<SmithyBuild>("doit") {
    addRuntimeClasspath = true
}

tasks["build"].finalizedBy(tasks["doit"])
