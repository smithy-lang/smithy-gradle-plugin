# Example Project - Source Projection

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This example demonstrates a simple Gradle Smithy project that places the default
source projection into the built jar.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.

## See Also

[Building a source model](https://smithy.io/2.0/guides/building-models/gradle-plugin.html#building-a-source-model)
