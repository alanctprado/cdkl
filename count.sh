#!/bin/bash

SAT_DIR="$(pwd)/pj1-tests/sat"
UNSAT_DIR="$(pwd)/pj1-tests/unsat"

for FILE in "$SAT_DIR"/*; do
	echo ""
	echo "$FILE"
	cat "$FILE" | grep "p cnf"
done
