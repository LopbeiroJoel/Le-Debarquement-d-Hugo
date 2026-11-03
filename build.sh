#!/bin/sh
set -eu
cd "$(dirname "$0")"
sh gradlew classes --console=plain
