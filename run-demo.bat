@echo off
echo [FastFloat] Building Native Library...
call compile.bat
if %ERRORLEVEL% NEQ 0 exit /b 1

echo [FastFloat] Building Core Project...
call mvn clean install -DskipTests -q
if %ERRORLEVEL% NEQ 0 exit /b 1

echo [FastFloat] Running Basic Usage Demo...
cd examples\00-basic-usage
call mvn compile exec:java -DskipTests
cd ..\..
pause
