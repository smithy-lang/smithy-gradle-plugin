rootProject.name = "multi-project"

include(":producer1")
include(":producer2")
include(":consumer")

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}
