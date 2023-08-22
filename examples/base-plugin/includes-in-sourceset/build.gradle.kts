plugins {
    id("java-library")
    id("smithy-base").version("0.8.0")
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
