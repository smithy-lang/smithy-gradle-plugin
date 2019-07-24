// This examples adds a Smithy tag to the built JAR.

plugins {
    java
    id("software.amazon.smithy").version("0.2.0")
}

group = "software.amazon.smithy"
version = "9.9.9"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.8.0")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    // You can add custom tags to your JAR that allow projections to search
    // for this JAR by tag and include this JAR as a projection source.
    tags = setOf("Foo", "Baz")
}
