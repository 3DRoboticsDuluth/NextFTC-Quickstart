@echo off
call "%~dp0scripts\adb.cmd" disconnect
if errorlevel 1 exit /b %ERRORLEVEL%
call "%~dp0scripts\adb.cmd" connect 192.168.43.1:5555
