@echo off
REM ============================================================
REM Compile Galaxy BBS Java sources into WEB-INF\classes
REM Run this from the project root (e.g. c:\ii\Galaxy_bbs)
REM ============================================================

set PROJECT_ROOT=%~dp0
if not "%PROJECT_ROOT:~-1%"=="" set PROJECT_ROOT=%PROJECT_ROOT:~0,-1%
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
javac -encoding UTF-8 -d "%OUT%" -cp "%CP%" -sourcepath "%SRC%" ^
  "%SRC%\controller\*.java" ^
  "%SRC%\dao\*.java" ^
  "%SRC%\filter\*.java" ^
  "%SRC%\model\*.java" ^
  "%SRC%\test\*.java" ^
  "%SRC%\util\*.java"

if %ERRORLEVEL% equ 0 (
    echo.
    echo Done. WEB-INF\classes has been built.
) else (
    echo.
    echo Compilation failed. Check errors above.
    exit /b 1
)
