# Smithy Gradle Plugins
[![Build Status](https://github.com/awslabs/smithy-gradle-plugin/workflows/ci/badge.svg)](https://github.com/awslabs/smithy-gradle-plugin/actions/workflows/ci.yml)

This project provides plugins to integrate [Smithy](https://smithy.io/) with [Gradle](https://gradle.org/). 
These plugins can build artifacts from Smithy models, generate JARs that contain Smithy models found in Java projects, 
and generate JARs that contain filtered projections of Smithy models.

- [`smithy-base` plugin](#smithy-base-plugin):  This plugin configures the basic source sets and configurations for a Smithy project. It also creates 
  the base `smithyBuild` task for the project that builds the Smithy models in the project.
- [`smithy-jar` plugin](#smithy-jar-plugin): Adds built Smithy files to an existing `jar` task such as that created by the Java or Kotlin plugins. 
  The `smithy-jar` plugin also adds build metadata and tags to the JAR's MANIFEST. The `smithy-jar` plugin applies the `smithy-base` plugin when it is applied.
- [`smithy-trait-package`](#smithy-trait-package-plugin): Configures a Java project for a custom trait definition. This plugin can be used in conjunction 
  with the [`trait-codegen`](https://github.com/smithy-lang/smithy/tree/main/smithy-trait-codegen) smithy build plugin to generate Java representations 
  of traits from Smithy IDL trait definitions. 
  The `smithy-trait-package` plugin applies both the `java-library` and `smithy-jar` plugins.

## Examples
Standalone examples are available for each of the provided plugins and can be found in the [examples](./examples) 
directory. In addition to serving as documentation, these examples are run as an integration tests for the plugins.

These examples can be copied into your workspace using the `smithy init` command line tool as follows:

```console
smithy init -t <EXAMPLE_NAME> -o <OUTPUT_DIRECTORY> --url https://github.com/smithy-lang/smithy-gradle-plugin
```

You can list all examples available in this repository as follows: 
```consoles
smithy init --list --url https://github.com/smithy-lang/smithy-gradle-plugin
```

**Note**: If you do not have the Smithy CLI installed, follow the [installation guide](https://smithy.io/2.0/guides/smithy-cli/cli_installation.html) 
to install it now.

## Guide
A detailed guide can be found here: https://smithy.io/2.0/guides/building-models/gradle-plugin.html

## Querying models in your project
All the Smithy Gradle plugins above will add a `select` task to your Gradle project. This task will execute the 
Smithy CLI `select` command, allowing you to queries a model using a [selector](https://smithy.io/2.0/spec/selectors.html#selectors).

For example, to query all trait definitions in your project you could use the `select` task as follows:
```console
gradle select --selector '[trait|trait]'
```

or with the Gradle wrapper:
```console
./gradlew select --selector '[trait|trait]'
```

## Plugins
### Smithy Base Plugin
The `smithy-base` plugin is a capability plugin primarily intended to be applied by other Smithy gradle plugins such as 
codegen plugins and the `smithy-jar` plugin.

This plugin sets up the `smithy` source set extension and the required `smithyCli` and 
`smithyBuild` configurations. The `smithy-base` plugin will also create a `smithyBuild` task 
to build the models for the main component of the project if a `main` sourceSet is configured 
for the project.

**Note**: The `smithy-base` plugin does not create any sourceSets on its own and will not set up
a `smithyBuild` task unless another plugin sets up a `main` sourceSet.


#### Usage
This plugin can be applied to a project as follows:
```kotlin 
// build.gradle.kts
plugins {
    id("software.amazon.smithy.gradle.smithy-base").version("1.2.0")
}
```
However, no tasks will be created unless.
See the [examples](./examples/base-plugin) directory for examples of using this plugin.

### Smithy Jar Plugin
The `smithy-jar` plugin will build Smithy models for a project and add Smithy models to an
existing `jar` task within the project. 

This plugin is primarily intended for the following use cases: 
- Including Smithy models alongside custom trait definitions in Java or another language.
- Packaging common Smithy models or validators into a JAR for consumption by other Smithy projects
- Including Smithy models in Smithy build plugin packages

#### Usage
The `smithy-jar` plugin must be used with another plugin that creates a `jar` task. For example, 

```kotlin 
// build.gradle.kts
plugins {
    id("java-library") // creates jar task
    id("software.amazon.smithy.gradle.smithy-jar").version("1.2.0")
}
```

See the [examples](./examples/jar-plugin) directory for examples of using this plugin.

### Smithy Trait Package Plugin
The `smithy-trait-package` plugin will configure a Java project for a custom Smithy trait. The `java-library` and `smithy-jar`
plugins are automatically applied and any traits generated with the `trait-codegen` build plugin will be added to the 
relevant sourceSets.

This plugin should be used to: 
- Create a package containing any combination of hand-written and code-generated custom trait definitions.
- Create a trait package with [custom validators](https://smithy.io/2.0/guides/model-linters.html#writing-custom-validators).

#### Usage
The `smithy-trait-package` plugin can be applied on its own to easily configure a trait package. 

```kotlin 
// build.gradle.kts
plugins {
    id("software.amazon.smithy.gradle.smithy-trait-package").version("1.2.0")
}
```

*Note*: To use the `trait-codegen` Smithy build plugin to generate Java trait definitions with this Gradle plugin,
users must still apply the plugin in their `smithy-build.json`.

*Note*: The `smithy-trait-package` plugin does not automatically apply any publishing plugins. If you wish to publish your
trait to Maven or another package repository you will need to add and configure a publishing plugin. We recommend using
the [jreleaser](https://jreleaser.org/) Gradle plugin to publish your package.

See the [examples](./examples/trait-package-plugin) directory for examples of using this plugin.

## Configuration
### Customizing source directories  
Smithy gradle plugins assumes Smithy model files (`*.smithy`) are organized in a similar way as Java source files, in sourceSets.
The `smithy-base` plugin adds a new sources block named `smithy` to every sourceSet. By default, this source block will include 
Smithy models in `model/`,`src/$sourceSetName/smithy` and`src/$sourceSetName/resources/META-INF/smithy`. New source directories can 
be added to a `smithy` sources block as follows:

```kotlin
// build.gradle.kts
sourceSets {
    main {
        smithy {
            srcDir("includes/")
        }
    }
}
```

### Customize output directory
By default, Smithy build artifacts will be placed in the project build directory
in a `smithyprojections/` directory. There are two ways to override the output directory. 
The first method is to set the `outputDirectory` property in the `smithy-build.json` config 
for your Smithy project. For example:

```json 
# smithy-build.json
{
    "outputDirectory": "build/output",
    ...
} 
```

The output directory can also be set explicitly configured for the plugin: 
```kotlin
// build.gradle.kts
smithy {
    outputDirectory.set(file("path/to/output"))
}
```


**Note**: Setting the output directory on the plugin extension will override any 
`outputDirectory` property set in the `smithy-build` config.


### Smithy Build Dependencies
The `smithy-base` plugin adds a `smithyBuild` configuration that can be used to 
specify dependencies that will only be used for calling smithy build but will 
not be included in any generated JARs. 

```kotlin 
// build.gradle.kts
dependencies {
    smithyBuild("com.example.software:build-only:1.0.0")
}
```


### Set `smithy-build` configs to use
By default, the plugin will look for a file called `smithy-build.json` at the 
project's root and will use that as the [`smithy-build`](https://smithy.io/2.0/guides/building-models/build-config.html)
config for your project. If no `smithy-build.json` file is found then a default 
empty build config is used to build the project.

Alternatively, you can explicitly configure one or more `smithy-build` configs to use 
for your project as follows:

```kotlin
// build.gradle.kts
smithy { 
    smithyBuildConfigs.set(files("smithy-build-config.json"))
}
```

### Set Smithy Tags to add to a JAR
When the `smithy-jar` plugin is applied to a project it can add a number of Smithy 
tags to the MANIFEST of a generated JAR. These tags can be used by downstream consumers
to filter which models to include in projections. Tags can be configured for the plugin 
as follows: 

```kotlin 
// build.gradle.kts 
smithy {
    tags.addAll("tag1", "anotherTag", "anotherTag2")
}
```

### Fork a new process when executing Smithy CLI commands
By default, Smithy CLI commands are run in the same process as Gradle, but inside a thread with a custom class loader. 
This should work in most cases, but there is an option to run inside a process if necessary. To run Smithy CLI commands
in a process set the `fork` configuration option to `true`: 

```kotlin 
// build.gradle.kts
smithy {
    fork.set(true)
}
```


### Disable Smithy Formatter
By default, the `smithy format` CLI command is executed on all source directories. 
This opinionated formatter follows the best practices recommended by the Smithy team. 
It is possible to disable the formatter by setting the `format` setting on the plugin 
extension to `false`: 

```kotlin
// build.gradle.kts 
smithy {
    format.set(false)
}
```

## Documentation

See https://smithy.io/2.0/guides/gradle-plugin/index.html


## License

This library is licensed under the Apache 2.0 License. 
