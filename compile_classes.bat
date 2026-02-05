@echo off
REM ============================================================
REM Compile Galaxy BBS Java sources into WEB-INF\classes
REM Run this from the project root (e.g. c:\ii\Galaxy_bbs)
REM ============================================================

setlocal

set "PROJECT_ROOT=%~dp0"
REM %~dp0 ends with a backslash; remove it for consistency
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
cd /d "%PROJECT_ROOT%"

REM Set Tomcat home - edit this if Tomcat is elsewhere
if "%CATALINA_HOME%"=="" set CATALINA_HOME=C:\tomcat10
if not exist "%CATALINA_HOME%\lib\servlet-api.jar" (
    echo ERROR: servlet-api.jar not found. Set CATALINA_HOME or edit this script.
    echo Example: set CATALINA_HOME=C:\tomcat10
    exit /b 1
)

set LIB=%PROJECT_ROOT%\WEB-INF\lib
set SRC=%PROJECT_ROOT%\WEB-INF\src
set OUT=%PROJECT_ROOT%\WEB-INF\classes
set CP=%LIB%\jakarta.activation-2.0.1.jar;%LIB%\jakarta.mail-2.0.1.jar;%LIB%\ojdbc8.jar;%CATALINA_HOME%\lib\servlet-api.jar

if not exist "%OUT%" mkdir "%OUT%"

echo Compiling Java sources to WEB-INF\classes ...
REM javac does NOT expand *.java globs on Windows. Build an @argfile of sources.
set "SRC_LIST=%TEMP%\galaxy_bbs_sources_%RANDOM%%RANDOM%.txt"
if exist "%SRC_LIST%" del /q "%SRC_LIST%"

for /r "%SRC%" %%F in (*.java) do (
  echo %%F>>"%SRC_LIST%"
)

javac -encoding UTF-8 -d "%OUT%" -cp "%CP%" -sourcepath "%SRC%" @"%SRC_LIST%"
set "JAVAC_EXIT=%ERRORLEVEL%"

del /q "%SRC_LIST%" >nul 2>&1

if %JAVAC_EXIT% equ 0 (
    echo.
    echo Done. WEB-INF\classes has been built.
) else (
    echo.
    echo Compilation failed. Check errors above.
    exit /b %JAVAC_EXIT%
)

endlocal
