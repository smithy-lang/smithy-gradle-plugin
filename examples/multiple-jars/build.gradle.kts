import software.amazon.smithy.gradle.actions.SmithyManifestUpdateAction
import software.amazon.smithy.gradle.tasks.SmithyBuildTask
import software.amazon.smithy.gradle.tasks.SmithyJarStagingTask

plugins {
    `java-library`
    id("software.amazon.smithy").version("0.7.0")
}

val buildTask: SmithyBuildTask = tasks.getByName<SmithyBuildTask>("smithyBuild")

// Stage smithy files for adding to jar
val stagingTask = tasks.create<SmithyJarStagingTask>("stageSmithySources") {
    inputDirectory.set(buildTask.outputDir)
    projection.set("sourceJar")
}

// Set up tasks that build source and javadoc jars.
tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    // Ensures the `smithy` directory is used not `META-INF`
    metaInf.from(stagingTask.smithyStagingDir.get())
    archiveClassifier.set("sources")
}

// Update the task with the smithy action
tasks["sourcesJar"].doFirst(SmithyManifestUpdateAction(project, setOf("a", "b", "c")))
tasks["sourcesJar"].dependsOn("stageSmithySources")
tasks["sourcesJar"].dependsOn("compileJava")

// Make sure the build task executes the sources Jar build
tasks["build"].dependsOn("sourcesJar")


repositories {
    mavenLocal()
    mavenCentral()
}
