import com.github.spotbugs.snom.Effort

plugins {
    id("smithy-gradle-plugin.java-conventions")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
    jacoco
    id("com.adarshr.test-logger")
    id("com.github.spotbugs")
}

/*
 * Common plugin settings
 * ====================================================
 */
gradlePlugin {
    website.set("https://smithy.io")
    vcsUrl.set("https://github.com/smithy-lang/smithy-gradle-plugin")
}

/*
 * Staging repository
 * ====================================================
 *
 * Configure publication to staging repo for jreleaser
 */
publishing {
    repositories {
        maven {
            name = "stagingRepository"
            url = uri("${rootProject.buildDir}/staging")
        }
    }
}

/*
 * Unit tests
 * ====================================================
 */
dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.0")
    testImplementation("org.hamcrest:hamcrest:2.1")
    testImplementation(project(":integ-test-utils"))
}

// Use Junit5's test runner.
tasks.withType<Test> {
    useJUnitPlatform()
}

testlogger {
    showExceptions = true
    showStackTraces = true
    showFullStackTraces = false
    showCauses = true
    showSummary = true
    showPassed = true
    showSkipped = true
    showFailed = true
    showOnlySlow = false
    showStandardStreams = true
    showPassedStandardStreams = false
    showSkippedStandardStreams = false
    showFailedStandardStreams = true
    logLevel = LogLevel.LIFECYCLE
}

/*
 * Configure integration tests
 * ====================================================
 */
sourceSets {
    create("it") {
        compileClasspath += sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath + sourceSets["test"].runtimeClasspath
    }
}
// Disable spotbugs and checkstyle for integration tests
tasks["spotbugsIt"].enabled = false
tasks["checkstyleIt"].enabled = false


tasks.register<Test>("integTest") {
    useJUnitPlatform()
    testClassesDirs = sourceSets["it"].output.classesDirs
    classpath = sourceSets["it"].runtimeClasspath
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2
}

afterEvaluate {
    tasks["integTest"].dependsOn("publishToMavenLocal")

    // Always run javadoc and integration tests after build.
    tasks["assemble"].dependsOn("javadoc")
    tasks["build"].finalizedBy(tasks["integTest"])
}

/*
 * Code coverage
 * ====================================================
 *
 * Create code coverage reports after running tests.
 */
// Always run the jacoco test report after testing.
tasks["test"].finalizedBy(tasks["jacocoTestReport"])
// Configure jacoco to generate an HTML report.
tasks.jacocoTestReport {
    reports {
        xml.required.set(false)
        csv.required.set(false)
        html.outputLocation.set(file("$buildDir/reports/jacoco"))
    }
}

/*
 * Spotbugs
 * ====================================================
 *
 * Run spotbugs against source files and configure suppressions.
 */
// Configure the spotbugs extension.
spotbugs {
    effort = Effort.MAX
    excludeFilter = file("${project.rootDir}/config/spotbugs/filter.xml")
}

// We don't need to lint tests.
tasks["spotbugsTest"].enabled = false

