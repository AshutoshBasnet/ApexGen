@echo off
echo ===================================================
echo  Launching ApexGen: Natural Selection Simulator
echo ===================================================

if not exist bin (
    call build.bat
)

java -cp bin com.evolution.ui.MainGUI
