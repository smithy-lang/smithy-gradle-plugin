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
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.4.8")
    implementation("com.adarshr:gradle-test-logger-plugin:4.0.0")

    // Plugin convention dependencies
    implementation("com.gradle.publish:plugin-publish-plugin:1.3.1")
}
