set -eu
cd "$(dirname "$0")"
sh build.sh
find src/test/java -name "*.java" > build/tests.txt
javac --release 8 -Xlint:-options -encoding UTF-8 -cp build -d build @build/tests.txt
java -Xmx512m -Djava.awt.headless=true -cp build:src/main/resources world.WorldTests
