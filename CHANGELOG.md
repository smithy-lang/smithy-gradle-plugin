# Smithy Gradle Plugin Changelog

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
