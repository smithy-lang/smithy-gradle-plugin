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

import org.jreleaser.model.Active

plugins {
    base
    id("org.jreleaser") version "1.9.0"
}

val pluginVersion = project.file("VERSION").readText().replace(System.lineSeparator(), "")
allprojects {
    group = "software.amazon.smithy.gradle"
    version = pluginVersion
}
println("Smithy Gradle version: '${pluginVersion}'")

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
                    stagingRepositories.add("${rootProject.buildDir}/staging")
                }
            }
        }
    }
}
