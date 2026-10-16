@echo off
setlocal DisableDelayedExpansion
rem KirinMaL signing setup for Lanz Magick. Uses the existing private key bundle.
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-signing.ps1" -LauncherDirectory "%~dp0." %*
exit /b %errorlevel%
