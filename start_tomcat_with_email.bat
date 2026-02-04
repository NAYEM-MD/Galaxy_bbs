@echo off
REM Sets SMTP env vars then starts Tomcat so welcome email works.
call "%~dp0set_smtp_env.bat"
echo Starting Tomcat with SMTP env vars...
cd /d c:\tomcat10\bin
call catalina start
echo Tomcat starting. Check this window for "[Galaxy_BBS] Welcome email sent" or error messages.
pause
