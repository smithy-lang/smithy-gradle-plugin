// This example adds a Smithy tag to the built JAR.

plugins {
    `java-library`
    id("software.amazon.smithy").version("0.8.0")
}

group = "software.amazon.smithy"
version = "9.9.9"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
}

smithy {
    // You can add custom tags to your JAR that allow projections to search
    // for this JAR by tag and include this JAR as a projection source.
    tags.addAll("Foo", "Baz")
}
