@echo off
REM Recipe Validator Script for Windows
REM This script runs the recipe validator

echo ========================================
echo   Create Immersive TACZ Recipe Validator
echo ========================================
echo.

REM Check if Python is installed
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    echo.
    echo Please install Python 3.6 or higher from:
    echo https://www.python.org/downloads/
    echo.
    pause
    exit /b 1
)

echo Running recipe validator...
echo.

python validate_recipes.py

if errorlevel 1 (
    echo.
    echo ========================================
    echo   VALIDATION FAILED
    echo ========================================
    echo.
    echo Errors found in recipes.
    echo Review the output above for details.
    echo.
    pause
    exit /b 1
) else (
    echo.
    echo ========================================
    echo   VALIDATION SUCCESSFUL
    echo ========================================
    echo.
    echo All recipes are valid!
    echo.
    pause
    exit /b 0
)
