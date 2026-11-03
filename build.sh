set -eu
cd "$(dirname "$0")"
mkdir -p build
find src/main/java -name "*.java" > build/sources.txt
javac --release 8 -Xlint:-options -encoding UTF-8 -d build @build/sources.txt
