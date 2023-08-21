// This example adds additional files to the main smithy source set.

plugins {
    id("java-library")
    id("smithy-base").version("0.7.0")
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
