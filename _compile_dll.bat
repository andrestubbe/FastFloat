@echo off
call "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\VC\Auxiliary\Build\vcvars64.bat"
cd /d C:\Users\andre\Documents\FastJava\2026-04-19-Work-FastFloat-v1.0
if not exist build mkdir build
cl.exe /O2 /W3 /MD /EHsc /fp:fast /I "C:\Program Files\Java\jdk-25\include" /I "C:\Program Files\Java\jdk-25\include\win32" /Fo:build\ /Fe:build\fastfloat.dll native\fastfloat.cpp /link /DLL /MACHINE:X64 /DEF:native\fastfloat.def
echo Exit Code: %ERRORLEVEL%
