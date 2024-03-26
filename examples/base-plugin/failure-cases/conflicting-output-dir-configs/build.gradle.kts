// This example attempts to set the output directory multiple times
// in different smithy-build.json files. The build will fail.

plugins {
    `java-library`
    id("software.amazon.smithy.gradle.smithy-base").version("1.0.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-aws-traits:[1.0, 2.0[")
}

smithy {
    smithyBuildConfigs.set(files("smithy-build.json", "smithy-build-conflicting.json"))
    format.set(false)
}
