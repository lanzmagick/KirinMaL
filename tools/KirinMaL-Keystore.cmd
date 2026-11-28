@echo off
setlocal EnableExtensions

rem KirinMaL: create one permanent JKS and its Base64 text beside this CMD.
set "OUTDIR=%~dp0"
set "KEYSTORE=%OUTDIR%KirinMaL-upload.jks"
set "BASE64=%OUTDIR%signing-secrets-KirinMaL.txt"
set "KEYTOOL="

rem Find keytool from JAVA_HOME, Android Studio JBR, or PATH.
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\keytool.exe" set "KEYTOOL=%JAVA_HOME%\bin\keytool.exe"
if not defined KEYTOOL if defined ANDROID_STUDIO_JBR if exist "%ANDROID_STUDIO_JBR%\bin\keytool.exe" set "KEYTOOL=%ANDROID_STUDIO_JBR%\bin\keytool.exe"
if not defined KEYTOOL if defined ProgramFiles if exist "%ProgramFiles%\Android\Android Studio\jbr\bin\keytool.exe" set "KEYTOOL=%ProgramFiles%\Android\Android Studio\jbr\bin\keytool.exe"
if not defined KEYTOOL if defined LOCALAPPDATA if exist "%LOCALAPPDATA%\Programs\Android Studio\jbr\bin\keytool.exe" set "KEYTOOL=%LOCALAPPDATA%\Programs\Android Studio\jbr\bin\keytool.exe"
if not defined KEYTOOL (
    for /f "delims=" %%K in ('where keytool.exe 2^>nul') do if not defined KEYTOOL set "KEYTOOL=%%K"
)

rem Never overwrite an existing signing key. Reuse it and refresh the Base64 text.
if exist "%KEYSTORE%" goto MAKE_BASE64

if not defined KEYTOOL (
    echo Java keytool was not found. Install Android Studio or a JDK, then run this CMD again.
    goto FAILED
)

echo.
echo KirinMaL permanent Android signing key
echo Alias: kirinmal
echo Password entry is invisible. Type the same password twice.
echo When asked for the key password, press ENTER to use the same password.
echo.

"%KEYTOOL%" -genkeypair -v ^
  -keystore "%KEYSTORE%" ^
  -storetype JKS ^
  -alias kirinmal ^
  -keyalg RSA ^
  -keysize 2048 ^
  -sigalg SHA256withRSA ^
  -validity 10000 ^
  -dname "CN=KirinMaL, OU=Android, O=LanzMagick, L=Kuala Lumpur, ST=Wilayah Persekutuan Kuala Lumpur, C=MY"

if errorlevel 1 (
    if exist "%KEYSTORE%" del /q "%KEYSTORE%" >nul 2>&1
    echo.
    echo Keystore generation failed.
    goto FAILED
)

:MAKE_BASE64
echo.
echo Creating Base64 text file beside this CMD...
set "KIRIN_KEYSTORE=%KEYSTORE%"
set "KIRIN_BASE64=%BASE64%"
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -Command "$b=[IO.File]::ReadAllBytes($env:KIRIN_KEYSTORE); [IO.File]::WriteAllText($env:KIRIN_BASE64,[Convert]::ToBase64String($b),[Text.Encoding]::ASCII)"
if errorlevel 1 (
    echo Could not create the Base64 text file.
    goto FAILED
)

if not exist "%BASE64%" (
    echo Base64 text file was not created.
    goto FAILED
)

echo.
echo Created:
echo %KEYSTORE%
echo %BASE64%
echo.
echo Notepad will open the TXT. Copy all text into GitHub Secret ANDROID_SIGNING_KEYSTORE_BASE64.
echo Set ANDROID_SIGNING_PASSWORD to the same password you entered.
start "" notepad.exe "%BASE64%"
exit /b 0

:FAILED
echo.
echo Setup failed. Keep any existing keystore safe.
pause
exit /b 1
