#!/bin/sh
set -eu
cd "$(dirname "$0")"
sh gradlew run --console=plain "$@"
