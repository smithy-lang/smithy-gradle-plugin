
plugins {
    `java-library`
    checkstyle
}

// Workaround per: https://github.com/gradle/gradle/issues/15383
val Project.libs get() = the<org.gradle.accessors.dm.LibrariesForLibs>()

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(libs.smithy.model)
    implementation(libs.smithy.build)
    implementation(libs.smithy.cli)
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
