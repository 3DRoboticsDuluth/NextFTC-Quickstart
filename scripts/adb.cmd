@echo off
setlocal

set "ADB="

for /f "delims=" %%A in ('where adb.exe 2^>nul') do if not defined ADB set "ADB=%%A"
if not defined ADB if defined ANDROID_SDK_ROOT if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" set "ADB=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
if not defined ADB if defined ANDROID_HOME if exist "%ANDROID_HOME%\platform-tools\adb.exe" set "ADB=%ANDROID_HOME%\platform-tools\adb.exe"
if not defined ADB if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"

if not defined ADB (
    echo ADB was not found. Add platform-tools to PATH or set ANDROID_SDK_ROOT. 1>&2
    exit /b 1
)

"%ADB%" %*
exit /b %ERRORLEVEL%
