@echo off
where python >nul 2>&1
if errorlevel 1 (
    py -3 "%~dp0scripts\preview-docs.py" %*
) else (
    python "%~dp0scripts\preview-docs.py" %*
)
exit /b %errorlevel%
