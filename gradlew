#!/bin/sh
set -eu
cd "$(dirname "$0")"
exec java --enable-native-access=ALL-UNNAMED -classpath gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain "$@"
