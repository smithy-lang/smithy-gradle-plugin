rootProject.name = "service-loader-classloading"

include(":service-loader-plugin")

pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        // Uncomment these to use the published version of the plugin from your preferred source.
        // gradlePluginPortal()
    }
}
