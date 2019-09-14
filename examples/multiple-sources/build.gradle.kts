// This example pulls Smithy models from the following locations:
// - model/
// - src/main/smithy/
// - src/main/resources/META-INF/smithy

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
