@echo off
echo ===================================================
echo  Compiling ApexGen: Natural Selection Simulator
echo ===================================================

if not exist bin mkdir bin

javac -encoding UTF-8 -d bin -sourcepath src src/com/evolution/model/*.java src/com/evolution/spatial/*.java src/com/evolution/engine/*.java src/com/evolution/ui/*.java src/com/evolution/test/*.java

if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Compilation complete. Output in bin/
) else (
    echo [ERROR] Compilation failed!
    exit /b %ERRORLEVEL%
)
