@echo off
set "SCRIPT_DIR=%~dp0"
if exist "%SCRIPT_DIR%..\.tools\maven\bin\mvn.cmd" (
    "%SCRIPT_DIR%..\.tools\maven\bin\mvn.cmd" %*
) else if exist "%SCRIPT_DIR%.tools\maven\bin\mvn.cmd" (
    "%SCRIPT_DIR%.tools\maven\bin\mvn.cmd" %*
) else (
    mvn %*
)
