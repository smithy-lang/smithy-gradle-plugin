# Example Project - Custom traits

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This project demonstrates the use of the `smithy-jar` gradle plugin for packaging
smithy trait definitions in a jar alongside the java definitions of those same custom
traits.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.

