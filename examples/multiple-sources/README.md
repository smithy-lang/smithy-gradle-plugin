# Example Project - Multiple Sources

This is an example Gradle Smithy project. In addition to serving as documentation,
this project is run as an integration test for the plugin.

This project demonstrates the three default locations where Smithy models are pulled
from in a given Gradle project:

- `model/`
- `src/main/smithy/`
- `src/main/resources/META-INF/smithy`

## Using the example as a starting point

Since this sample is run as an integration test, by default it is only configured
to use a locally published version of the plugin. To use this as a starting point
for your own project, uncomment the lines in `settings.gradle.kts` that configure
Gradle to use public sources.

## See Also

[Smithy model sources](https://awslabs.github.io/smithy/1.0/guides/building-models/gradle-plugin.html#smithy-model-sources)

[`smithy-build.json`'s `imports`](https://awslabs.github.io/smithy/1.0/guides/building-models/build-config.html?highlight=imports)
