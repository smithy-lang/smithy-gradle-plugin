plugins {
    id("java-library")
    id("smithy-jar").version("0.8.0")
}


// This test project doesn't produce a JAR.
tasks["jar"].enabled = false

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(project(":custom-string-trait"))
}