// This example attempts to use an invalid projection. The build will fail.

plugins {
    `java-library`
    id("smithy-base").version("0.7.0")
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
