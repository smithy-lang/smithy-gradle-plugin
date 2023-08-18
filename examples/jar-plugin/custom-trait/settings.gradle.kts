rootProject.name = "custom-string-trait"

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        // Uncomment these to use the published version of the plugin from your preferred source.
        // gradlePluginPortal()
    }
}

include("custom-string-trait")
include("consumer")
