@echo off
REM ============================================================
REM Download required JARs (not in Git) to WEB-INF\lib
REM Run from project root. Requires PowerShell.
REM Oracle ojdbc8.jar must be added manually (Oracle license).
REM ============================================================

setlocal
set "LIB=%~dp0WEB-INF\lib"
if not exist "%LIB%" mkdir "%LIB%"

echo.
echo [Galaxy BBS] Downloading dependencies to WEB-INF\lib...
echo.

set "MAVEN=https://repo1.maven.org/maven2"

REM jBCrypt (bcrypt password hashing)
if not exist "%LIB%\jbcrypt-0.4.jar" (
    echo Downloading jbcrypt-0.4.jar...
    powershell -NoProfile -Command "Invoke-WebRequest -Uri '%MAVEN%/org/mindrot/jbcrypt/0.4/jbcrypt-0.4.jar' -OutFile '%LIB%\jbcrypt-0.4.jar' -UseBasicParsing"
    if exist "%LIB%\jbcrypt-0.4.jar" (echo   OK) else (echo   FAILED)
) else (
    echo jbcrypt-0.4.jar already exists.
)

REM Jakarta Mail 2.x (jakarta.mail.* package - API + impl)
set "MAIL_JAR="
for %%f in ("%LIB%\jakarta.mail*.jar" "%LIB%\angus-mail*.jar") do set "MAIL_JAR=1"
if not defined MAIL_JAR (
    echo Downloading jakarta.mail-2.0.2.jar...
    powershell -NoProfile -Command "Invoke-WebRequest -Uri '%MAVEN%/com/sun/mail/jakarta.mail/2.0.2/jakarta.mail-2.0.2.jar' -OutFile '%LIB%\jakarta.mail-2.0.2.jar' -UseBasicParsing"
    if exist "%LIB%\jakarta.mail-2.0.2.jar" (echo   OK) else (echo   FAILED)
) else (
    echo jakarta.mail already exists.
)

echo.
echo NOTE: ojdbc8.jar (Oracle JDBC) is NOT auto-downloaded.
echo       Add it manually to WEB-INF\lib from Oracle or Maven.
echo.
echo Run: compile_classes.bat check   to verify all JARs
echo.
