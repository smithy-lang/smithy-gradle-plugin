# Example Project - Build Dependencies

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This project demonstrates the use of smithy build dependencies to specify dependencies 
that will only be used for calling smithy build but will not be included in any generated 
jars.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.

## See Also

[Generating a projection](https://smithy.io/2.0/guides/building-models/gradle-plugin.html#generating-a-projection)

