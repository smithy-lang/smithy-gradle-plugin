# Smithy Gradle Plugin Changelog

## 1.4.0 (2026-02-23)

### Fixes
* Fixed an issue that prevented failures with smithyFormat to be reported correctly. ([#161](https://github.com/smithy-lang/smithy-gradle-plugin/pull/161))
* Fixed a test case that was incorrectly configured. ([#160](https://github.com/smithy-lang/smithy-gradle-plugin/pull/160))

### Features
* Updated the select command to set its classpath to allow model discovery in the dependency closure. ([#159](https://github.com/smithy-lang/smithy-gradle-plugin/pull/159))

## 1.3.0 (2025-06-10)

### Features
* Updated validate and format tasks to be incremental, which greatly reduces subsequent build times. ([#155](https://github.com/smithy-lang/smithy-gradle-plugin/pull/155), [#156](https://github.com/smithy-lang/smithy-gradle-plugin/pull/156))

### Fixes
* Fixed internal dependency resolution behavior when plugin is used within [smithy-lang/smithy](https://github.
  com/smithy-lang/smithy). [(#151)](https://github.com/smithy-lang/smithy-gradle-plugin/pull/151) 

## 1.2.0 (2025-01-02)

### Features 
* Updated minimum severity of validation events in `validate` task to `DANGER`. ([#148](https://github.com/smithy-lang/smithy-gradle-plugin/pull/148))

### Documentation 
* Added documentation and examples for using the `smithy-trait-package` plugin. ([#146](https://github.com/smithy-lang/smithy-gradle-plugin/pull/146))



## 1.1.0 (2024-07-16)

### Features 
* Added a convenience method to `SmithyBuild` task to resolve plugin projection directories. ([#140](https://github.com/smithy-lang/smithy-gradle-plugin/pull/140))
* Added a new gradle plugin `smithy-trait-package` to enable users to quickly create packages for custom Smithy traits.  ([#138](https://github.com/smithy-lang/smithy-gradle-plugin/pull/138), [#139](https://github.com/smithy-lang/smithy-gradle-plugin/pull/139), [#141](https://github.com/smithy-lang/smithy-gradle-plugin/pull/141))

## 1.0.0 (2024-03-26)

### Features 
* Added `SmithySelectTask` to enable use of `select` command within Gradle projects ([#135](https://github.com/smithy-lang/smithy-gradle-plugin/pull/135)) 
* Merged the`SmithyBaseTask` with the `AbstractSmithyCliTask` to remove unecessary indirection ([#134](https://github.com/smithy-lang/smithy-gradle-plugin/pull/134))
* Refactored the `SmithyValidateTask` task to be under the `smithy-base` plugin to enable the use of the validation task outside of `smithy-jar` task ([#133](https://github.com/smithy-lang/smithy-gradle-plugin/pull/133))
* Added a severity option to the `validate` task to allow setting a minimum severity to print. ([#131](https://github.com/smithy-lang/smithy-gradle-plugin/pull/131))
* Added an exception on implicit empty smithy-build config. ([#123](https://github.com/smithy-lang/smithy-gradle-plugin/pull/123), [#128](https://github.com/smithy-lang/smithy-gradle-plugin/pull/128))
* Made the `smithyCli` configuration visible to enable pinning of the CLI version. ([#130](https://github.com/smithy-lang/smithy-gradle-plugin/pull/130))

### Bug Fixes
* Added a quiet flag to smithy CLI commands when gradle quiet flag is set to stop printing validation events when quieted. ([#132](https://github.com/smithy-lang/smithy-gradle-plugin/pull/132))


## 0.10.1 (2024-03-12)

### Features 
* Updated JAR plugin to add Smithy resources to compile classpath. ([#124](https://github.com/smithy-lang/smithy-gradle-plugin/pull/124))

### Bug Fixes
* Removed version check for smithy format task. ([#125](https://github.com/smithy-lang/smithy-gradle-plugin/pull/125))

### Documentation 
* Updated description of Smithy init templates. ([#122](https://github.com/smithy-lang/smithy-gradle-plugin/pull/122))


## 0.10.0 (2024-01-12)

### Features 
* Decoupled creation of `smithyCli` configuration from sourceSets ([#112](https://github.com/smithy-lang/smithy-gradle-plugin/pull/112))
* Refactored build scripts to use convention plugins for common configuration ([#117](https://github.com/smithy-lang/smithy-gradle-plugin/pull/117))
* Added Jreleaser configuration ([#115](https://github.com/smithy-lang/smithy-gradle-plugin/pull/115))
* Updated Javadocs and suppressed doclint warnings to reduce noise during builds ([#116](https://github.com/smithy-lang/smithy-gradle-plugin/pull/116))
* Updated `getProjectionPluginPath` utility method to allow resolution of the path even if file does not yet exist ([#114](https://github.com/smithy-lang/smithy-gradle-plugin/pull/114))

### Bug Fixes
* Corrected error in build task parameter conventions ([#112](https://github.com/smithy-lang/smithy-gradle-plugin/pull/112))
* Corrected `SmithyBuildTask` to use `InputFiles` for build config input to ensure correct behavior of incremental builds ([#111](https://github.com/smithy-lang/smithy-gradle-plugin/pull/111))
* Updated `SmithyExtension` to use comment-safe parsing method when parsing build configs ([#110](https://github.com/smithy-lang/smithy-gradle-plugin/pull/110))

## 0.9.0 (2023-10-20)

### Features
* Added Scala support for the `smithy-jar` plugin ([#104](https://github.com/smithy-lang/smithy-gradle-plugin/pull/104))
* Updated task ordering to support use of annotation processors and execute format task before build task ([#103](https://github.com/smithy-lang/smithy-gradle-plugin/pull/103))

### Bug Fixes
* Corrected resolution of CLI dependency version from resolved runtime dependencies ([#105](https://github.com/smithy-lang/smithy-gradle-plugin/pull/105)) 

## 0.8.0 (2023-08-22)

### Features
* Add `smithy-templates.json` to allow use of examples with the smithy init cli tool ([#96](https://github.com/smithy-lang/smithy-gradle-plugin/pull/96))
* Add script to automatically update version numbers in examples and documentation ([#92](https://github.com/smithy-lang/smithy-gradle-plugin/pull/92))
* Remove legacy plugin in favor of `smithy-base` and `smithy-jar` plugins ([#91](https://github.com/smithy-lang/smithy-gradle-plugin/pull/91))
* Add integration tests for newly added `smithy-jar` and `smithy-base` plugins ([#89](https://github.com/smithy-lang/smithy-gradle-plugin/pull/89), [#90](https://github.com/smithy-lang/smithy-gradle-plugin/pull/90))
* Add `smithy-jar` convention plugin to add Smithy files to existing `:jar` tasks ([#86](https://github.com/smithy-lang/smithy-gradle-plugin/pull/86))
* Add `smithy-base` capability plugin ([#85](https://github.com/smithy-lang/smithy-gradle-plugin/pull/85))
* Refactor repository to allow for multiple plugins ([#82](https://github.com/smithy-lang/smithy-gradle-plugin/pull/82), [#84](https://github.com/smithy-lang/smithy-gradle-plugin/pull/84))
* Upgrade to Gradle to 8.2.0 ([#83](https://github.com/smithy-lang/smithy-gradle-plugin/pull/83))

### Bug Fixes
* Correct behavior of multi-project example and add missing integration test ([#93](https://github.com/smithy-lang/smithy-gradle-plugin/pull/93))

### Documentation
* Update repository README to reflect plugin refactor ([#97](https://github.com/smithy-lang/smithy-gradle-plugin/pull/97))
* Update Javadocs ([#94](https://github.com/smithy-lang/smithy-gradle-plugin/pull/92))
* Update example documentation ([#95](https://github.com/smithy-lang/smithy-gradle-plugin/pull/95))

## 0.7.0 (2023-04-13)

### Features
* Update Plugin-publish to 1.2.0 ([#77](https://github.com/awslabs/smithy-gradle-plugin/pull/77))
* Update SpotBugs to 5.0.14 ([#77](https://github.com/awslabs/smithy-gradle-plugin/pull/77))
* Include license information in generated POM ([#76](https://github.com/awslabs/smithy-gradle-plugin/pull/76))
* Upgrade Gradle to 7.4.2 ([#71](https://github.com/awslabs/smithy-gradle-plugin/pull/71))
* Allow for short-form license header ([#69](https://github.com/awslabs/smithy-gradle-plugin/pull/69))
* Update documentation to point to smithy.io ([#66](https://github.com/awslabs/smithy-gradle-plugin/pull/66))
* Support implicit dependencies ([#57](https://github.com/awslabs/smithy-gradle-plugin/pull/57))
* Update Gradle plugin to work with new Smithy CLI ([#54](https://github.com/awslabs/smithy-gradle-plugin/pull/54))

### Bug Fixes

* Correctly bind extensions to tasks ([#75](https://github.com/awslabs/smithy-gradle-plugin/pull/75))
* Fix modifiesLogging test to handle new logging in Validate command ([#70](https://github.com/awslabs/smithy-gradle-plugin/pull/70))
* Ensure that stderrr/stdout are printed by each integration test ([#55](https://github.com/awslabs/smithy-gradle-plugin/pull/55))
* Fix buildDir not being respected ([#52](https://github.com/awslabs/smithy-gradle-plugin/pull/52))

## 0.6.0 (2021-12-01)

### Features

* Upgraded to Gradle 7.1. ([#43](https://github.com/awslabs/smithy-gradle-plugin/pull/43))

### Bug Fixes

* Fixed duplicate JAR entry issue. ([#43](https://github.com/awslabs/smithy-gradle-plugin/pull/43))
* Fixed the issue where Smithy-Tags would disappear when `jar` task was re-run. ([#47](https://github.com/awslabs/smithy-gradle-plugin/pull/47))

## 0.5.3 (2021-05-05)

* Fixed the `ZipFile invalid LOC header (bad signature)` error that was caused by
  changing resource files. ([#40](https://github.com/awslabs/smithy-gradle-plugin/pull/40))
* Upgrade to Gradle 6. ([#38](https://github.com/awslabs/smithy-gradle-plugin/pull/38))
* Added documentation to plugin examples. ([#35](https://github.com/awslabs/smithy-gradle-plugin/pull/35))
* Fixed plugin path resolution when an `outputDirectory` has been configured.
  ([#31](https://github.com/awslabs/smithy-gradle-plugin/pull/31))

## 0.5.2 (2020-09-30)

### Bug Fixes

* Fixed an issue where the system classpath separator was not used. ([#28](https://github.com/awslabs/smithy-gradle-plugin/pull/28))

## 0.5.1 (2020-05-27)

### Bug Fixes

* Fix the behavior of building empty projects so that when no models can be
  found, the Gradle plugin warns instead of fails. ([#24](https://github.com/awslabs/smithy-gradle-plugin/pull/24))
  
### Features

* Scan for a smithy-cli buildScript dependency when building models if no explicit
  smithy-cli or smithy-model dependency can be found. ([#25](https://github.com/awslabs/smithy-gradle-plugin/pull/25))
