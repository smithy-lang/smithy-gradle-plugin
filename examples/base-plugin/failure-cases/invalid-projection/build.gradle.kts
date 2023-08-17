// This example attempts to use an invalid projection. The build will fail.

plugins {
    id("java-library")
    id("smithy-base").version("0.7.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
}

smithy {
    projection.set("invalid")
    format.set(false)
}
