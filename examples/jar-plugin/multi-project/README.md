# Example Project - Trait Dependencies

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This example demonstrates how you can use dependencies in your model. In particular,
it demonstrates the definition and usage of two custom traits in separate packages.
It also showcases the usage of the plugin with sub-projects.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.
