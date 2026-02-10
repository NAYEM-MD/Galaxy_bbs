@echo off
REM ============================================================
REM Compile Galaxy BBS Java sources into WEB-INF\classes
REM Run this from the project root (e.g. c:\ii\Galaxy_bbs)
REM
REM Optional: DB connectivity test (Oracle)
REM   compile_classes.bat dbtest [DB_URL] [DB_USER] [DB_PASSWORD]
REM Example:
REM   compile_classes.bat dbtest "jdbc:oracle:thin:@//192.168.1.50:1521/orclpdb1" Galaxy_bbs pro
REM Or set env vars before running:
REM   set DB_URL=...
REM   set DB_USER=Galaxy_bbs
REM   set DB_PASSWORD=pro
REM   compile_classes.bat dbtest
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

REM Optional Oracle DB connection test
if /i "%~1"=="dbtest" (
    if not "%~2"=="" set "DB_URL=%~2"
    if not "%~3"=="" set "DB_USER=%~3"
    if not "%~4"=="" set "DB_PASSWORD=%~4"

    echo.
    echo Checking Oracle DB connection via util.DBUtil ...
    set "TMP_DIR=%TEMP%\galaxy_bbs_dbtest_%RANDOM%%RANDOM%"
    if not exist "%TMP_DIR%" mkdir "%TMP_DIR%"

    set "PING_SRC=%TMP_DIR%\DbPing.java"
    >"%PING_SRC%" (
        echo import java.sql.Connection;
        echo import util.DBUtil;
        echo public class DbPing ^{
        echo   public static void main(String[] args) ^{
        echo     try ^(Connection c = DBUtil.getConnection^(^)^) ^{
        echo       if ^(c == null^) ^{ System.err.println^("DB connection is null."^); System.exit^(2^); ^}
        echo       System.out.println^("DB connected OK: " + c.getMetaData^(^).getURL^(^)^);
        echo     ^} catch ^(Exception e^) ^{
        echo       System.err.println^("DB connect FAILED: " + e.getMessage^(^)^);
        echo       e.printStackTrace^(^);
        echo       System.exit^(1^);
        echo     ^}
        echo   ^}
        echo ^}
    )

    javac -encoding UTF-8 -d "%TMP_DIR%" -cp "%OUT%;%CP%" "%PING_SRC%"
    if not %ERRORLEVEL% equ 0 (
        echo DB test compile failed.
        rd /s /q "%TMP_DIR%" >nul 2>&1
        exit /b 1
    )

    java -cp "%TMP_DIR%;%OUT%;%CP%" DbPing
    set "DBTEST_EXIT=%ERRORLEVEL%"

    rd /s /q "%TMP_DIR%" >nul 2>&1

    if not %DBTEST_EXIT% equ 0 (
        echo.
        echo DB connection test failed with exit code %DBTEST_EXIT%.
        exit /b %DBTEST_EXIT%
    )
)

endlocal
