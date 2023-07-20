import software.amazon.smithy.gradle.tasks.SmithyBuildTask

// This example builds Smithy models using a custom build task
// and disables the default "smithyBuildJar" task. This allows
// for a more granular level of control in when the build runs
// and the classpath used when building.

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
    mavenCentral()
}
