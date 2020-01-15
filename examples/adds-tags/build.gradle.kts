// This examples adds a Smithy tag to the built JAR.

plugins {
    id("software.amazon.smithy").version("0.4.2")
}

group = "software.amazon.smithy"
version = "9.9.9"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:0.9.7")
}

configure<software.amazon.smithy.gradle.SmithyExtension> {
    // You can add custom tags to your JAR that allow projections to search
    // for this JAR by tag and include this JAR as a projection source.
    tags = setOf("Foo", "Baz")
}
