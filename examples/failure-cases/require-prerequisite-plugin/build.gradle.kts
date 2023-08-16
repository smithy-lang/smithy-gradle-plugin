// This example attempts to use the smithy-jar plugin without a required
// prerequisite plugin. The build will fail.

plugins {
    id("smithy-jar").version("0.7.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}
