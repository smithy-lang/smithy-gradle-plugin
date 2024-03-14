// This example adds additional files to the main smithy source set.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-base").version("0.10.1")
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
