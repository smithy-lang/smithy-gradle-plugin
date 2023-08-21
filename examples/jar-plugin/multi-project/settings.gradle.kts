rootProject.name = "multi-project"

include(":producer1")
include(":producer2")
include(":consumer")

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        // Uncomment these to use the published version of the plugin from your preferred source.
        // gradlePluginPortal()
    }
}
