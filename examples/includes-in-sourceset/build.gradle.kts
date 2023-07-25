import software.amazon.smithy.gradle.tasks.SmithyBuildTask

sourceSets {
    main {
        smithy {
            srcDir("includes/")
        }
    }
}

plugins {
    `java-library`
    id("software.amazon.smithy").version("0.7.0")
}

repositories {
    mavenLocal()
    //
    mavenCentral()
}
