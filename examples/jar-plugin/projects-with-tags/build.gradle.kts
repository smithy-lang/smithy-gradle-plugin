// This example places a projected version of the model into the JAR.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-jar").version("0.9.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    smithyBuild(files("jars/a/a.jar", "jars/b/b.jar"))
    implementation(files("jars/c/c.jar"))
}

smithy {
    sourceProjection.set("foo")
    projectionSourceTags.addAll("X", "Foo")
}
