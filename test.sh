#!/bin/bash

SAT_DIR="$(pwd)/pj1-tests/sat"
UNSAT_DIR="$(pwd)/pj1-tests/unsat"

./gradlew build

for FILE in "$SAT_DIR"/*; do
	echo ""
	echo "$FILE"
	{ time (timeout 5 ./gradlew run --args="$FILE"); } 2>&1
done
