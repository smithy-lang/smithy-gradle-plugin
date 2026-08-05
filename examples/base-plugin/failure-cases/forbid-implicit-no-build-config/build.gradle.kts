plugins {
    `java-library`
    id("software.amazon.smithy.gradle.smithy-base").version("1.5.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}
