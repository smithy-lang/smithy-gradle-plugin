# Example Project - Custom Smithy Build Task

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This example builds Smithy models using a custom build task and disables the default
`smithyBuild` task. This allows for a more granular level of control of when the
build runs, as well as the classpath used when building.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.
