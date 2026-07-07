# Example Project - Format Check

This is an example Gradle Smithy project. In addition to serving as
documentation, this project is run as an integration test for the plugin.

This example demonstrates the `smithyFormat` check mode. The model in `model/`
is valid but intentionally left unformatted. Running
`gradle smithyFormat --check` fails the build and lists the files that would
be reformatted, without modifying them. Running `gradle smithyFormat` with no
property reformats the model in place.

Check mode requires Smithy CLI 1.72.0 or later, which is why this example pins
that version.

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only
configured to use a locally published version of the plugin. To use this as a
starting point for your own project, uncomment the lines in
`settings.gradle.kts` that configure Gradle to use public sources.
