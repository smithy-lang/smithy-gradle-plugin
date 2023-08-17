// This example fails to build due to a syntax error.

plugins {
    id("java-library")
    id("smith-base").version("0.7.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}

smithy {
    format.set(false)
}