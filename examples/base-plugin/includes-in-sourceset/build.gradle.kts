// This example adds additional files to the main smithy source set.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-base").version("1.3.0")
}

sourceSets {
    main {
        smithy {
            srcDir("includes/")
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}
