@echo off
REM ============================================================
REM Compile Galaxy BBS Java sources into WEB-INF\classes
REM Run this from the project root (e.g. c:\tomcat10\webapps\Galaxy_bbs)
REM JSPs use JSTL (jars in WEB-INF\lib); this compiles servlets/models/DAOs.
REM
REM Usage:
REM   compile_classes.bat           - compile only
REM   compile_classes.bat check     - verify required JARs exist
REM   compile_classes.bat dbtest [DB_URL] [DB_USER] [DB_PASSWORD]
REM
REM Set CATALINA_HOME if Tomcat is not at C:\tomcat10
REM ============================================================

setlocal enabledelayedexpansion

set "PROJECT_ROOT=%~dp0"
if "%PROJECT_ROOT:~-1%"=="\" set "PROJECT_ROOT=%PROJECT_ROOT:~0,-1%"
cd /d "%PROJECT_ROOT%"

set LIB=%PROJECT_ROOT%\WEB-INF\lib

REM Optional: check required JARs (run: compile_classes.bat check)
if /i not "%~1"=="check" goto do_compile
echo.
echo [Galaxy BBS] Checking required JARs in WEB-INF\lib...
set "MISSING=0"
set "FOUND_OJ="
set "FOUND_BC="
set "FOUND_MAIL="
for %%f in ("%LIB%\ojdbc*.jar") do set "FOUND_OJ=1"
for %%f in ("%LIB%\jbcrypt*.jar") do set "FOUND_BC=1"
for %%f in ("%LIB%\*mail*.jar") do set "FOUND_MAIL=1"
if not defined FOUND_OJ goto check_missing_oj
echo   [OK] Oracle JDBC
goto check_bc
:check_missing_oj
echo   [MISSING] ojdbc8.jar or ojdbc11.jar - Oracle JDBC driver
echo            Get from: Oracle Maven or oracle.com
set "MISSING=1"
:check_bc
if not defined FOUND_BC goto check_missing_bc
echo   [OK] jBCrypt
goto check_mail
:check_missing_bc
echo   [MISSING] jbcrypt-0.4.jar - bcrypt hashing
echo            Run: download_deps.bat  OR  Maven: org.mindrot:jbcrypt:0.4
set "MISSING=1"
:check_mail
if not defined FOUND_MAIL goto check_missing_mail
echo   [OK] Mail API
goto check_done
:check_missing_mail
echo   [MISSING] jakarta.mail / angus-mail - for email (welcome, password reset)
echo            Maven: org.eclipse.angus:angus-mail:2.0.2  +  jakarta.mail-api
set "MISSING=1"
:check_done
if %MISSING%==1 goto check_fail
echo.
echo All required JARs found.
exit /b 0
:check_fail
echo.
echo Add missing JARs to WEB-INF\lib\  then run compile_classes.bat
exit /b 1
:do_compile

REM Set Tomcat home - edit this if Tomcat is elsewhere
if "%CATALINA_HOME%"=="" set CATALINA_HOME=C:\tomcat10
set "TC_LIB=%CATALINA_HOME%\lib"
if not exist "%TC_LIB%" goto err_no_tomcat
goto tomcat_ok
:err_no_tomcat
echo ERROR: Tomcat lib not found: %TC_LIB%
echo Set CATALINA_HOME or edit this script. Example: set CATALINA_HOME=C:\tomcat10
exit /b 1
:tomcat_ok
REM Tomcat 9: servlet-api.jar; Tomcat 10+: jakarta.servlet-api-*.jar
set "SERVLET_JAR="
if exist "%TC_LIB%\servlet-api.jar" set "SERVLET_JAR=%TC_LIB%\servlet-api.jar"
if "%SERVLET_JAR%"=="" for %%j in ("%TC_LIB%\jakarta.servlet-api*.jar") do set "SERVLET_JAR=%%j"
if not "%SERVLET_JAR%"=="" goto servlet_ok
echo ERROR: No servlet-api.jar or jakarta.servlet-api*.jar in %TC_LIB%
exit /b 1
:servlet_ok

set LIB=%PROJECT_ROOT%\WEB-INF\lib
set SRC=%PROJECT_ROOT%\WEB-INF\src
set OUT=%PROJECT_ROOT%\WEB-INF\classes
set "ARGDIR=%PROJECT_ROOT%\WEB-INF"
REM Build classpath: project libs + all Tomcat lib jars (for Jakarta EE / Part, etc.)
set "CP="
if exist "%LIB%" for %%j in ("%LIB%\*.jar") do set "CP=!CP!;%%j"
for %%j in ("%TC_LIB%\*.jar") do set "CP=!CP!;%%j"
if "!CP:~0,1!"==";" set "CP=!CP:~1!"

if not exist "%OUT%" mkdir "%OUT%"

REM Use project dir for temp files (avoids %TEMP% path/handle issues). No long command line: use javac @argfile.
set "SRC_LIST=%ARGDIR%\sources_list.txt"
set "JAVAC_ARGS=%ARGDIR%\javac_args.txt"
if exist "%SRC_LIST%" del /q "%SRC_LIST%"
if exist "%JAVAC_ARGS%" del /q "%JAVAC_ARGS%"

REM Build source list with dir (reliable; no for-loop redirect issues)
dir /s /b "%SRC%\*.java" 2>nul >"%SRC_LIST%"
if not exist "%SRC_LIST%" goto no_sources
findstr /r "." "%SRC_LIST%" >nul 2>&1
if errorlevel 1 goto no_sources
goto have_sources
:no_sources
echo ERROR: No .java files found under %SRC%
exit /b 1
:have_sources

REM Write full javac args to file to avoid Windows command-line length limit and quoting issues
REM Use forward slashes so javac does not interpret \t etc. in paths (Java accepts / on Windows)
set "OUT_FWD=!OUT:\=/!"
set "SRC_FWD=!SRC:\=/!"
set "CP_FWD=!CP:\=/!"
echo -encoding> "%JAVAC_ARGS%"
echo UTF-8>> "%JAVAC_ARGS%"
echo -d>> "%JAVAC_ARGS%"
echo !OUT_FWD!>> "%JAVAC_ARGS%"
echo -sourcepath>> "%JAVAC_ARGS%"
echo !SRC_FWD!>> "%JAVAC_ARGS%"
echo -cp>> "%JAVAC_ARGS%"
echo !CP_FWD!>> "%JAVAC_ARGS%"
REM Append all .java paths (one per line) - use forward slashes to avoid \t etc.
for /f "usebackq delims=" %%a in ("%SRC_LIST%") do (
  set "line=%%a"
  set "line=!line:\=/!"
  echo !line!>> "%JAVAC_ARGS%"
)

echo Compiling Java sources to WEB-INF\classes ...
javac @"%JAVAC_ARGS%"
set "JAVAC_EXIT=%ERRORLEVEL%"

del /q "%SRC_LIST%" >nul 2>&1
del /q "%JAVAC_ARGS%" >nul 2>&1

if %JAVAC_EXIT% equ 0 goto compile_ok
echo.
echo Compilation failed. Check errors above.
exit /b %JAVAC_EXIT%
:compile_ok
echo.
echo Done. WEB-INF\classes built successfully.

REM Optional Oracle DB connection test
if /i not "%~1"=="dbtest" goto script_end
if not "%~2"=="" set "DB_URL=%~2"
if not "%~3"=="" set "DB_USER=%~3"
if not "%~4"=="" set "DB_PASSWORD=%~4"
echo.
echo Checking Oracle DB connection via util.DBUtil ...
set "TMP_DIR=%TEMP%\galaxy_bbs_dbtest_%RANDOM%%RANDOM%"
if not exist "%TMP_DIR%" mkdir "%TMP_DIR%"
set "PING_SRC=%TMP_DIR%\DbPing.java"
echo import java.sql.Connection; > "%PING_SRC%"
echo import util.DBUtil; >> "%PING_SRC%"
echo public class DbPing { >> "%PING_SRC%"
echo   public static void main(String[] args) { >> "%PING_SRC%"
echo     try (Connection c = DBUtil.getConnection()) { >> "%PING_SRC%"
echo       if (c == null) { System.err.println("DB connection is null."); System.exit(2); } >> "%PING_SRC%"
echo       System.out.println("DB connected OK: " + c.getMetaData().getURL()); >> "%PING_SRC%"
echo     } catch (Exception e) { >> "%PING_SRC%"
echo       System.err.println("DB connect FAILED: " + e.getMessage()); >> "%PING_SRC%"
echo       e.printStackTrace(); >> "%PING_SRC%"
echo       System.exit(1); >> "%PING_SRC%"
echo     } >> "%PING_SRC%"
echo   } >> "%PING_SRC%"
echo } >> "%PING_SRC%"
javac -encoding UTF-8 -d "%TMP_DIR%" -cp "%OUT%;%CP%" "%PING_SRC%"
if not %ERRORLEVEL% equ 0 goto dbtest_compile_fail
java -cp "%TMP_DIR%;%OUT%;%CP%" DbPing
set "DBTEST_EXIT=%ERRORLEVEL%"
rd /s /q "%TMP_DIR%" >nul 2>&1
if %DBTEST_EXIT% equ 0 goto script_end
echo.
echo DB connection test failed with exit code %DBTEST_EXIT%.
exit /b %DBTEST_EXIT%
:dbtest_compile_fail
echo DB test compile failed.
rd /s /q "%TMP_DIR%" >nul 2>&1
exit /b 1
:script_end
endlocal
exit /b 0
