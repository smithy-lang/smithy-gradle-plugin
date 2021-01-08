# Example Project - Build Script Dependency

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This example demonstrates using the version of Smithy found in the build script
dependencies to build the project.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.
