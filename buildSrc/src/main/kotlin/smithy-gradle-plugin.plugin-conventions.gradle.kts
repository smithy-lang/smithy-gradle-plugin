import com.github.spotbugs.snom.Effort

plugins {
    id("smithy-gradle-plugin.java-conventions")
    `java-gradle-plugin`
    id("com.gradle.plugin-publish")
    jacoco
    id("com.adarshr.test-logger")
    id("com.github.spotbugs")
}

// Workaround per: https://github.com/gradle/gradle/issues/15383
val Project.libs get() = the<org.gradle.accessors.dm.LibrariesForLibs>()

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
            url = uri(stagingDir())
        }
    }
    // Add license spec to all maven publications
    publications.withType<MavenPublication>() {
        project.afterEvaluate {
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/smithy-lang/smithy-gradle-plugin")
                licenses {
                    license {
                        name.set("Apache License 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("smithy")
                        name.set("Smithy")
                        organization.set("Amazon Web Services")
                        organizationUrl.set("https://aws.amazon.com")
                        roles.add("developer")
                    }
                }
                scm {
                    url.set("https://github.com/smithy-lang/smithy-gradle-plugin.git")
                }
            }
        }
    }
}

/*
 * Unit tests
 * ====================================================
 */
dependencies {
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.hamcrest)
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

dependencies {
    // JUnit's classes carry @API(status = ...) annotations defined in the compile-only
    // apiguardian artifact. The `it` source set draws its compile classpath from
    // testRuntimeClasspath, which omits compile-only deps, so add apiguardian directly to
    // stop javac warning spam.
    "itCompileOnly"(libs.apiguardian.api)
}

// Disable spotbugs for integration tests
tasks["spotbugsIt"].enabled = false


tasks.register<Test>("integTest") {
    useJUnitPlatform()
    testClassesDirs = sourceSets["it"].output.classesDirs
    classpath = sourceSets["it"].runtimeClasspath
    maxParallelForks = Runtime.getRuntime().availableProcessors() / 2

    // Optionally run the integ tests against a specific Gradle version. When unset,
    // they use the version running this task (the wrapper). CI sets this to exercise
    // consumers against our declared minimum and the current release while still
    // building with the wrapper.
    providers.gradleProperty("gradleTestVersion").orNull?.let {
        systemProperty("gradleTestVersion", it)
    }
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
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
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

