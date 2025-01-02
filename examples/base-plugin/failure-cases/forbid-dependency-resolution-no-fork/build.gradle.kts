// This example tries to use dependencies within the smithy-build.json file.
// The build will fail.

plugins {
    `java-library`
    id("software.amazon.smithy.gradle.smithy-base").version("1.2.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

smithy {
    format.set(false)
}
