# Example Project - Custom Output Directory

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This example demonstrates how to write the built Smithy models to a custom output
directory using the `outputDirectory` property of the `smithy-build` config

**Note**: Setting the output directory via the `smithy` gradle plugin extension will 
override any output Directory settings in provided smithy-build configs.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.
