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

plugins {
    `java-library`
    `maven-publish`
    checkstyle
    jacoco
    id("com.github.spotbugs") version "5.0.14"
    id("com.gradle.plugin-publish") version "1.2.0"
    id("com.adarshr.test-logger") version "3.2.0"
}

// The root project doesn't produce a JAR.
tasks["jar"].enabled = false

val pluginVersion = project.file("VERSION").readText().replace(System.lineSeparator(), "")
allprojects {
    group = "software.amazon.smithy"
    version = pluginVersion
}
println("Smithy Gradle version: '${pluginVersion}'")


subprojects {
    val subproject = this

    apply(plugin = "java-gradle-plugin")
    apply(plugin = "com.gradle.plugin-publish")

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

    // Reusable license copySpec
    val licenseSpec = copySpec {
        from("${project.rootDir}/LICENSE")
        from("${project.rootDir}/NOTICE")
    }

    dependencies {
        implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
        implementation("software.amazon.smithy:smithy-build:[1.0, 2.0[")
        implementation("software.amazon.smithy:smithy-cli:[1.0, 2.0[")

        testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.0")
        testImplementation("org.hamcrest:hamcrest:2.1")
    }

    // Set up tasks that build source and javadoc jars.
    tasks.register<Jar>("sourcesJar") {
        metaInf.with(licenseSpec)
        from(sourceSets.main.get().allJava)
        archiveClassifier.set("sources")
    }

    tasks.register<Jar>("javadocJar") {
        metaInf.with(licenseSpec)
        from(tasks.javadoc)
        archiveClassifier.set("javadoc")
    }

    // Configure jars to include license related info
    tasks.jar {
        metaInf.with(licenseSpec)
        manifest {
            attributes["Automatic-Module-Name"] = "software.amazon.smithy.gradle"
        }
    }

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

    tasks["integTest"].dependsOn("publishToMavenLocal")

    // Always run javadoc and integration tests after build.
    tasks["assemble"].dependsOn("javadoc")
    tasks["build"].finalizedBy(tasks["integTest"])

    /*
     * Maven
     * ====================================================
     *
     * Publish to Maven central.
     */
    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>("pluginMaven") {
                pom {
                    description.set(subproject.description)
                    url.set("https://github.com/awslabs/smithy-gradle-plugin")
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
                        url.set("https://github.com/awslabs/smithy-gradle-plugin.git")
                    }
                }
            }
        }

        repositories {
            mavenLocal()
            mavenCentral()
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

    /*
     * Gradle plugins
     * ====================================================
     */
    // Include an Automatic-Module-Name in all JARs.
    afterEvaluate {
        val proj = this
        gradlePlugin {
            plugins {
                create("software.amazon.smithy") {
                    id = proj.extra["id"].toString()
                    displayName = proj.extra["displayName"].toString()
                    description = proj.description
                    implementationClass = proj.extra["implementationClass"].toString()
                }
            }
        }
    }

    pluginBundle {
        website = "https://github.com/smithy-lang/smithy"
        vcsUrl = "https://github.com/smithy-lang/smithy"
        tags = listOf("smithy", "api", "building")
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }
}



