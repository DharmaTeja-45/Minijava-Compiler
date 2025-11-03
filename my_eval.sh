#!/bin/bash

OUTPUT="output"
TEMP="temp.miniIR"
TEMP_OUTPUT="temp.txt"
EXPECTED_OUT="exp.txt"
ERROR_LOG="error_log"
LAB_NO=$1
PGI="pgi.jar"


echo "evaluating.. 4"

mkdir -p bin
mkdir -p "$OUTPUT"

if [ ! -f ./P4/P4.java ]
then 
    echo "File not found"
    exit 1
else 
    echo "Compiling P4.java..."
    javac -sourcepath ./P4 -d bin ./P4/P4.java 
    
    # Check if compilation was successful
    if [ $? -ne 0 ]; then
        echo "Compilation failed"
        exit 1
    fi
    
    echo "Running test cases..."
    for test_case in ./public_test_cases_miniIR/*
    do 
        if [ -f "$test_case" ]; then
            echo "Running test: $(basename $test_case)"
            java -jar "$PGI" < "$test_case" > "$EXPECTED_OUT"
            java -cp bin ./P4/P4 < "$test_case" &> "./$OUTPUT/$(basename $test_case).microIR"
            java -jar "$PGI" < "./$OUTPUT/$(basename $test_case).microIR" > "$TEMP_OUTPUT" 
            diff -q "$EXPECTED_OUT" "$TEMP_OUTPUT" && echo "PASSED" || echo "FAILED" 
        fi
    done
fi

