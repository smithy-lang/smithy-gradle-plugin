rootProject.name = "build-dependencies"

pluginManagement {
    repositories {
        mavenLocal()
        // Uncomment these to use the published version of the plugin from your preferred source.
        // gradlePluginPortal()
        // mavenCentral()
    }
}

include(":service")
include(":internal-model")
