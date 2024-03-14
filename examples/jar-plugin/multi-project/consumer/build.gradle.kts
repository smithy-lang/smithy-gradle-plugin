plugins {
    id("software.amazon.smithy.gradle.smithy-jar").version("0.10.1")
}

dependencies {
    implementation(project(":producer1"))
    implementation(project(":producer2"))
}
