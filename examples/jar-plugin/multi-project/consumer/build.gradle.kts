plugins {
    id("software.amazon.smithy.gradle.smithy-jar").version("1.0.0")
}

dependencies {
    implementation(project(":producer1"))
    implementation(project(":producer2"))
}
