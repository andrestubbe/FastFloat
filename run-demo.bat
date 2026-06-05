@echo off
echo ðŸš€ Running Hero Demo...
cd examples\00-basic-usage
call mvn -q compile exec:java -Dexec.mainClass=fastfloat.example.Demo
cd ..\..
pause
