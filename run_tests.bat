@echo off
setlocal enabledelayedexpansion
REM Run unit tests (ValidationUtil, PasswordUtil, FileTypeUtil).
REM Requires: compile_classes.bat has been run, JUnit in WEB-INF\lib (run download_deps.bat).
set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
cd /d "%PROJECT_ROOT%"

set "LIB=%PROJECT_ROOT%\WEB-INF\lib"
set "CLASSES=%PROJECT_ROOT%\WEB-INF\classes"
set "CP=%CLASSES%"
for %%j in ("%LIB%\*.jar") do set "CP=!CP!;%%j"
if "%CATALINA_HOME%"=="" set CATALINA_HOME=C:\tomcat10
for %%j in ("%CATALINA_HOME%\lib\*.jar") do set "CP=!CP!;%%j"

echo Running unit tests...
java -cp "%CP%" org.junit.runner.JUnitCore test.ValidationUtilTest test.PasswordUtilTest test.FileTypeUtilTest
echo.
echo Done.
endlocal
