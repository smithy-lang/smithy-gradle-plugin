
plugins {
    `java-library`
    id("smithy-gradle-plugin.formatting-conventions")
}

// Workaround per: https://github.com/gradle/gradle/issues/15383
val Project.libs get() = the<org.gradle.accessors.dm.LibrariesForLibs>()

// Compile production sources against the Java 8 API so the plugins keep supporting
// Java 8 consumers.
tasks.compileJava {
    options.release.set(8)
}

// JUnit 6 requires Java 17, so tests are compiled against the Java 17 API even though
// the production sources they exercise remain Java 8 compatible.
tasks.compileTestJava {
    options.release.set(17)
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
 * Repositories
 * ====================================================
 */
repositories {
    mavenLocal()
    mavenCentral()
}
