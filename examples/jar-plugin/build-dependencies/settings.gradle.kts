rootProject.name = "build-dependencies"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        // Uncomment these to use the published version of the plugin from your preferred source.
        // gradlePluginPortal()
    }
}

include(":service")
include(":internal-model")
