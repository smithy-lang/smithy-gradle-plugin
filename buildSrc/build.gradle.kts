plugins {
    // Support convention plugins written in Kotlin. Convention plugins are
    // build scripts in 'src/main' that automatically become available as
    // plugins in the main build.
    `kotlin-dsl`
}

repositories {
    // Use the plugin portal to apply community plugins in convention plugins.
    gradlePluginPortal()
}

dependencies {
    // Java convention dependencies
    implementation(libs.spotbugs)
    implementation(libs.test.logger)

    // Plugin convention dependencies
    implementation(libs.plugin.publish)

    // Make the generated version catalog accessors (LibrariesForLibs) available
    // to precompiled script plugins. https://github.com/gradle/gradle/issues/15383
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
