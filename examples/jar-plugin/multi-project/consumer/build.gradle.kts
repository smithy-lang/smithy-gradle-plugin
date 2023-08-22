plugins {
    id("smithy-jar").version("0.8.0")
}

dependencies {
    implementation(project(":producer1"))
    implementation(project(":producer2"))
}
