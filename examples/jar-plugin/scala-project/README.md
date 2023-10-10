# Example Project - Scala Project

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This project demonstrates using the Smithy Gradle Plugin with a Scala project. 
The Jar created by the Scala plugin will include the Smithy models built by the 
`smithyBuild` task.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.

