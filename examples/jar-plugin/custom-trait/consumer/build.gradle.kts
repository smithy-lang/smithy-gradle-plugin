plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-jar").version("1.3.0")
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

smithy {
    smithyBuildConfigs.set(project.files())
}
