#!/usr/bin/env bash

EXPECTED_VERSION=$(cat VERSION)

# find all gradle build files and replace the existing version with the expected
find . -type f -name 'build.gradle.kts' \
  -exec sed -i '' "s/\(id(\"smithy-\([[:lower:]]*\)\")\.version(\"\)\([[:digit:]\.]*\)\")/\\1${EXPECTED_VERSION}\")/" {} \;

# update all references to the version in READMEs
find . -type f -name 'README.md' \
  -exec sed -i '' "s/\(id(\"smithy-\([[:lower:]]*\)\")\.version(\"\)\([[:digit:]\.]*\)\")/\\1${EXPECTED_VERSION}\")/" {} \;
