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

// Prevent compatibility issues if an unsupported JDK is configured as default
kotlin {
    jvmToolchain(17)
}

dependencies {
    // Java convention dependencies
    implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.0.6")
    implementation("com.adarshr:gradle-test-logger-plugin:4.0.0")

    // Plugin convention dependencies
    implementation("com.gradle.publish:plugin-publish-plugin:1.2.1")
}
