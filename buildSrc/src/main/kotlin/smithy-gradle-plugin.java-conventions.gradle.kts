
plugins {
    `java-library`
    checkstyle
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
    implementation("software.amazon.smithy:smithy-build:[1.0, 2.0[")
    implementation("software.amazon.smithy:smithy-cli:[1.0, 2.0[")
}

//// ==== Licensing =====
// Reusable license copySpec
val licenseSpec = copySpec {
    from("${project.rootDir}/LICENSE")
    from("${project.rootDir}/NOTICE")
}

// Configure all jars to include license info
tasks.withType<Jar>() {
    metaInf.with(licenseSpec)
}



// Suppress warnings in javadocs
tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:-html", "-quiet")
}

/*
 * CheckStyle
 * ====================================================
 *
 * Apply CheckStyle to source files but not tests.
 */
tasks["checkstyleTest"].enabled = false

/*
 * Repositories
 * ====================================================
 */
repositories {
    mavenLocal()
    mavenCentral()
}
