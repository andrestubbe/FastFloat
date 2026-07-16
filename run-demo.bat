@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo [FastFloat] Running Demo (via JitPack)...
cd examples\00-basic-usage
call mvn compile exec:java -Dexec.mainClass=fastfloat.example.Demo
cd ..\..
pause
