// This example exercises the `smithyFormatCheck` task. The model is valid but
// intentionally left unformatted, so `gradle smithyFormatCheck` fails without
// modifying it, while a plain `gradle smithyFormat` reformats it in place.
//
// The CLI version is pinned to 1.72.0 because that is the first release with
// `smithy format --check`.

plugins {
    id("java-library")
    id("software.amazon.smithy.gradle.smithy-base").version("1.4.0")
}

dependencies {
    smithyCli("software.amazon.smithy:smithy-cli:1.72.0")
}

repositories {
    mavenLocal()
    mavenCentral()
}
