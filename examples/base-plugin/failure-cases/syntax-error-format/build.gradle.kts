// This example fails because smithyFormat is run on a model with a syntax error.
// format is intentionally left enabled (the default) to exercise the smithyFormat task path.
// the build must fail with the real syntax error message, not a secondary NoClassDefFoundError 
// caused by smithy-model types escaping the isolated classloader.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-base").version("1.4.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}
