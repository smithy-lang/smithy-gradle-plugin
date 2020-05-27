plugins {
    id("software.amazon.smithy").version("0.5.1")
}

dependencies {
    implementation(project(":producer1"))
    implementation(project(":producer2"))
}
