# Example Project - Projection Sources with Tags

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This example demonstrates how to use the tags set on Smithy model JARs to control
which sources are used for a projection. See the `Adding Tags` example to see how
to add those tags to the JAR.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.

## See Also

[Projection tags](https://awslabs.github.io/smithy/1.0/guides/building-models/gradle-plugin.html#projection-tags)
