/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import com.adarshr.gradle.testlogger.TestLoggerExtension
import org.jreleaser.model.Active

plugins {
    `java-library`
    jacoco
    id("com.github.spotbugs") version "5.0.14"
    id("com.adarshr.test-logger") version "3.2.0"
    id("com.gradle.plugin-publish") version "1.2.1" apply false
    id("org.jreleaser") version "1.9.0"
}

// The root project doesn't produce a JAR.
tasks["jar"].enabled = false

val pluginVersion = project.file("VERSION").readText().replace(System.lineSeparator(), "")
allprojects {
    group = "software.amazon.smithy.gradle"
    version = pluginVersion
}
println("Smithy Gradle version: '${pluginVersion}'")

// JReleaser publishes artifacts from a local staging repository, rather than maven local.
// https://jreleaser.org/guide/latest/examples/maven/staging-artifacts.html#_gradle
val stagingDirectory = "$buildDir/staging"

subprojects {
    val subproject = this

    if (subproject.name != "integ-test-utils") {
        apply(plugin = "java-gradle-plugin")
        apply(plugin = "com.gradle.plugin-publish")
    } else {
        apply(plugin = "java-library")
    }

    /*
    * Java
    * ====================================================
    */

    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // Use Junit5's test runner.
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    // Suppress warnings in javadocs
    tasks.withType<Javadoc> {
        (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:-html", "-quiet")
    }

    apply(plugin = "com.adarshr.test-logger")
    configure<TestLoggerExtension> {
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

    dependencies {
        implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
        implementation("software.amazon.smithy:smithy-build:[1.0, 2.0[")
        implementation("software.amazon.smithy:smithy-cli:[1.0, 2.0[")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.0")
        testImplementation("org.hamcrest:hamcrest:2.1")
        testImplementation(project(":integ-test-utils"))
    }

    // Reusable license copySpec
    val licenseSpec = copySpec {
        from("${project.rootDir}/LICENSE")
        from("${project.rootDir}/NOTICE")
    }

    if (subproject.name != "integ-test-utils") {
        // Configure all jars to include license info
        tasks.withType<Jar>() {
            metaInf.with(licenseSpec)
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
         * Common plugin settings
         * ====================================================
         */
        apply(plugin = "com.gradle.plugin-publish")
        configure<GradlePluginDevelopmentExtension> {
            website.set("https://smithy.io")
            vcsUrl.set("https://github.com/smithy-lang/smithy-gradle-plugin")
        }

        /*
         * Staging repository
         * ====================================================
         *
         * Configure publication to staging repo for jreleaser
         */
        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "stagingRepository"
                    url = uri(stagingDirectory)
                }
            }
        }

        /*
         * CheckStyle
         * ====================================================
         *
         * Apply CheckStyle to source files but not tests.
         */
        apply(plugin = "checkstyle")
        tasks["checkstyleTest"].enabled = false
        tasks["checkstyleIt"].enabled = false

        /*
         * Code coverage
         * ====================================================
         *
         * Create code coverage reports after running tests.
         */
        apply(plugin = "jacoco")
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
        apply(plugin = "com.github.spotbugs")
        // We don't need to lint tests.
        tasks["spotbugsTest"].enabled = false
        tasks["spotbugsIt"].enabled = false

        // Configure the bug filter for spotbugs.
        tasks.withType<SpotBugsTask>().configureEach {
            effort.set(Effort.MAX)
            excludeFilter.set(project.file("${project.rootDir}/config/spotbugs/filter.xml"))
        }
    }

    /*
     * Repositories
     * ====================================================
     */
    repositories {
        mavenLocal()
        mavenCentral()
    }
}


/*
 * Jreleaser (https://jreleaser.org) config.
 */
jreleaser {
    dryrun = false

    // Used for creating a tagged release, uploading files and generating changelog.
    // In the future we can set this up to push release tags to GitHub, but for now it's
    // set up to do nothing.
    // https://jreleaser.org/guide/latest/reference/release/index.html
    release {
        generic {
            enabled = true
            skipRelease = true
        }
    }

    // Used to announce a release to configured announcers.
    // https://jreleaser.org/guide/latest/reference/announce/index.html
    announce {
        active = Active.NEVER
    }

    // Signing configuration.
    // https://jreleaser.org/guide/latest/reference/signing.html
    signing {
        active = Active.ALWAYS
        armored = true
    }

    // Configuration for deploying to Maven Central.
    // https://jreleaser.org/guide/latest/examples/maven/maven-central.html#_gradle
    deploy {
        maven {
            nexus2 {
                create("maven-central") {
                    active = Active.ALWAYS
                    url = "https://aws.oss.sonatype.org/service/local"
                    snapshotUrl = "https://aws.oss.sonatype.org/content/repositories/snapshots"
                    closeRepository.set(false)
                    releaseRepository.set(false)
                    stagingRepositories.add(stagingDirectory)
                }
            }
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}
