# Smithy Gradle Plugin Changelog

## 0.5.3 (TBD)

* Fixed the `ZipFile invalid LOC header (bad signature)` error that was caused by
  changing resource files.

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
