// This example attempts to use an invalid projection. The build will fail.

plugins {
    id("software.amazon.smithy").version("0.4.3")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.9.7")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    projection = "invalid"
}
