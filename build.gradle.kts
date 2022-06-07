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
import com.adarshr.gradle.testlogger.TestLoggerExtension;

plugins {
    `java-gradle-plugin`
    `maven-publish`
    checkstyle
    jacoco
    id("com.github.spotbugs") version "4.7.1"
    id("com.gradle.plugin-publish") version "0.11.0"
    id("com.adarshr.test-logger") version "3.2.0"
}

group = "software.amazon.smithy"
version = "0.6.0"

dependencies {
    implementation("software.amazon.smithy:smithy-model:[1.0, 2.0[")
    implementation("software.amazon.smithy:smithy-build:[1.0, 2.0[")
    implementation("software.amazon.smithy:smithy-cli:[1.0, 2.0[")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.4.0")
    testImplementation("org.hamcrest:hamcrest:2.1")
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

repositories {
    mavenLocal()
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            // Ship the source and javadoc jars.
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
        }
    }
}

/*
 * CheckStyle
 * ====================================================
 *
 * Apply CheckStyle to source files but not tests.
 */

tasks["checkstyleTest"].enabled = false
tasks["checkstyleIt"].enabled = false

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
        xml.isEnabled = false
        csv.isEnabled = false
        html.destination = file("$buildDir/reports/jacoco")
    }
}

/*
 * Spotbugs
 * ====================================================
 *
 * Run spotbugs against source files and configure suppressions.
 */

// We don't need to lint tests.
tasks["spotbugsTest"].enabled = false
tasks["spotbugsIt"].enabled = false

// Configure the bug filter for spotbugs.
tasks.withType<SpotBugsTask>().configureEach {
    effort.set(Effort.MAX)
    excludeFilter.set(project.file("config/spotbugs/filter.xml"))
}

/*
 * Gradle plugins
 * ====================================================
 */

gradlePlugin {
    plugins {
        create("software.amazon.smithy") {
            id = "software.amazon.smithy"
            displayName = "Smithy Gradle Plugin"
            description = "This project integrates Smithy with Gradle. This plugin can build artifacts " +
                    "from Smithy models, generate JARs that contain Smithy models found in Java " +
                    "projects, and generate JARs that contain filtered *projections* of Smithy " +
                    "models."
            implementationClass = "software.amazon.smithy.gradle.SmithyPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/awslabs/smithy"
    vcsUrl = "https://github.com/awslabs/smithy"
    tags = listOf("smithy", "api", "building")
}
