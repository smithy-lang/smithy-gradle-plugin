// This example places a projected version of the model into the JAR.

plugins {
    `java-library`
    id("software.amazon.smithy").version("0.7.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    smithyBuildDep(files("jars/a/a.jar", "jars/b/b.jar", "jars/c/c.jar"))
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
}

smithy {
    sourceProjection.set("foo")
    projectionSourceTags.addAll("X", "Foo")
}
