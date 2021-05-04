# Smithy Gradle Plugin
[![Build Status](https://github.com/awslabs/smithy-gradle-plugin/workflows/ci/badge.svg)](https://github.com/awslabs/smithy-gradle-plugin/actions/workflows/ci.yml)

This project integrates Smithy with Gradle. This plugin can build artifacts
from Smithy models, generate JARs that contain Smithy models found in Java
projects, and generate JARs that contain filtered *projections* of Smithy
models.


## Installation

The Smithy Gradle plugin is applied using the `software.amazon.smithy` plugin.
The following example configures a project to use the Smithy Gradle plugin:

```kotlin
plugins {
    id("software.amazon.smithy").version("0.5.3")
}
```


## Documentation

See https://awslabs.github.io/smithy/1.0/guides/building-models/gradle-plugin.html


## License

This library is licensed under the Apache 2.0 License. 
