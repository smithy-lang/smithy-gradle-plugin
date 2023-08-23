// This example attempts to use the smithy-jar plugin without a required
// prerequisite plugin. The build will fail.

plugins {
    id("software.amazon.smithy-jar").version("0.8.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}
