#!/bin/bash
cd "$(dirname "$0")"
javac -d . util/*.java entity/*.java particle/*.java enemy/*.java ability/*.java manager/*.java ui/*.java core/*.java
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi
echo "Compilation successful! Starting game..."
java -cp . core.Game
