// This example attempts to use an invalid projection. The build will fail.

plugins {
    id("software.amazon.smithy").version("0.3.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.9.0")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    projection = "invalid"
}
