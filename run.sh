#!/bin/bash
set -e
cd "$(dirname "$0")"
export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"

mkdir -p out data
echo "Compiling..."
find src -name '*.java' > sources.txt
javac -d out @sources.txt
echo "Launching SLCAS..."
java -cp out Main
