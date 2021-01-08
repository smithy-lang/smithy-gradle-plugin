# Example Project - No Models

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This example demonstrates a project with no defined models.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.

Since this example has no models, you will need to add them yourself. To do this,
create a directory called `model` and add your new model files there. See the
`Multiple Sources` project or the documentation [here](https://awslabs.github.io/smithy/1.0/guides/building-models/gradle-plugin.html#smithy-model-sources)
for other locations models are read from by default. Models may also be placed in
custom locations using [`imports` in your `smithy-build.json`](https://awslabs.github.io/smithy/1.0/guides/building-models/build-config.html)
