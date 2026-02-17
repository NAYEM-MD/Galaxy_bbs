@echo off
setlocal
cd /d "%~dp0"

:menu
cls
echo.
echo  ========== Galaxy BBS - Main Menu ==========
echo.
echo  1. Download dependencies (jbcrypt, jakarta.mail)
echo  2. Check required JARs
echo  3. Compile
echo  4. Compile + DB connection test
echo  5. Set SMTP env (email) for this window
echo  6. Start Tomcat (with SMTP if set)
echo  7. Full setup: download + compile + check
echo  0. Exit
echo.
set /p "choice=Choose (0-7): "

if "%choice%"=="0" exit /b 0
if "%choice%"=="1" goto download
if "%choice%"=="2" goto check
if "%choice%"=="3" goto compile
if "%choice%"=="4" goto dbtest
if "%choice%"=="5" goto smtp
if "%choice%"=="6" goto tomcat
if "%choice%"=="7" goto full

echo Invalid choice.
timeout /t 2 >nul
goto menu

:download
call "%~dp0download_deps.bat"
pause
goto menu

:check
call "%~dp0compile_classes.bat" check
pause
goto menu

:compile
call "%~dp0compile_classes.bat"
pause
goto menu

:dbtest
call "%~dp0compile_classes.bat" dbtest
pause
goto menu

:smtp
call "%~dp0set_smtp_env.bat"
echo.
echo SMTP vars are set in THIS window. Use option 6 to start Tomcat in same window.
pause
goto menu

:tomcat
call "%~dp0set_smtp_env.bat"
echo.
if "%CATALINA_HOME%"=="" set "CATALINA_HOME=C:\tomcat10"
echo Starting Tomcat...
cd /d "%CATALINA_HOME%\bin"
call catalina start
cd /d "%~dp0"
echo.
echo Tomcat started. Open http://localhost:8080/Galaxy_bbs/
pause
goto menu

:full
call "%~dp0download_deps.bat"
echo.
call "%~dp0compile_classes.bat" check
if errorlevel 1 (
    echo Add missing JARs (especially ojdbc8.jar) then try again.
    pause
    goto menu
)
call "%~dp0compile_classes.bat"
echo.
echo Full setup done. Run SQL/schema.sql in Oracle, then option 6 to start Tomcat.
pause
goto menu
