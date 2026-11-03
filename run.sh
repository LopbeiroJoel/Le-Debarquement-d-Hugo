set -eu
cd "$(dirname "$0")"
sh build.sh
java -cp build:src/main/resources Main
