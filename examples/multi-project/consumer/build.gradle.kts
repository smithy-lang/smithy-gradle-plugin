plugins {
    id("software.amazon.smithy").version("0.5.2")
}

dependencies {
    implementation(project(":producer1"))
    implementation(project(":producer2"))
}
