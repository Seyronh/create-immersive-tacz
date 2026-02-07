#!/bin/bash
# Recipe Validator Script for Linux/Mac
# This script runs the recipe validator

echo "========================================"
echo "  Create Immersive TACZ Recipe Validator"
echo "========================================"
echo ""

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    if ! command -v python &> /dev/null; then
        echo "ERROR: Python is not installed or not in PATH"
        echo ""
        echo "Please install Python 3.6 or higher."
        echo ""
        exit 1
    else
        PYTHON_CMD="python"
    fi
else
    PYTHON_CMD="python3"
fi

echo "Running recipe validator..."
echo ""

$PYTHON_CMD validate_recipes.py

if [ $? -ne 0 ]; then
    echo ""
    echo "========================================"
    echo "  VALIDATION FAILED"
    echo "========================================"
    echo ""
    echo "Errors found in recipes."
    echo "Review the output above for details."
    echo ""
    exit 1
else
    echo ""
    echo "========================================"
    echo "  VALIDATION SUCCESSFUL"
    echo "========================================"
    echo ""
    echo "All recipes are valid!"
    echo ""
    exit 0
fi
