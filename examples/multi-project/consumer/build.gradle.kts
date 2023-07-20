plugins {
    id("software.amazon.smithy").version("0.8.0")
}

dependencies {
    implementation(project(":producer1"))
    implementation(project(":producer2"))
}
