@echo off
setlocal EnableExtensions DisableDelayedExpansion
rem KirinMaL - Lanz Magick. Interactive Android signing-key generator.
rem SPDX-License-Identifier: GPL-3.0-or-later
title KirinMaL - Jana Signing Key

echo KirinMaL - Jana key sendiri
echo.
echo Skrip ini menjana KEY BAHARU dalam folder KirinMaL-key-sendiri.
echo Key baharu tidak boleh update APK yang menggunakan signing key lama.
echo Alias dan butiran sijil sudah diisi. Kau hanya masukkan password.
echo.

set "KIRINMAL_KEYTOOL=keytool.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\keytool.exe" (
    set "KIRINMAL_KEYTOOL=%JAVA_HOME%\bin\keytool.exe"
    goto keytool_ready
)
where.exe /q keytool.exe
if errorlevel 1 goto need_java

:keytool_ready
where.exe /q powershell.exe
if errorlevel 1 goto need_powershell
set "KIRINMAL_OUTPUT=%~dp0KirinMaL-key-sendiri"
set "KIRINMAL_JKS=%KIRINMAL_OUTPUT%\KirinMaL-release.jks"
set "KIRINMAL_BASE64=%KIRINMAL_OUTPUT%\KEYSTORE_FILE.txt"
if exist "%KIRINMAL_JKS%" goto already_exists

set "KIRINMAL_ALIAS=kirinmal"

if not exist "%KIRINMAL_OUTPUT%" mkdir "%KIRINMAL_OUTPUT%"
if not exist "%KIRINMAL_OUTPUT%" goto output_failed
echo.
echo Isi password keystore dan ulang untuk pengesahan.
echo Password tidak dipaparkan ketika ditaip. Gunakan sekurang-kurangnya 6 aksara.
echo Tekan Enter pada soalan password key jika mahu sama dengan password keystore.
echo.
"%KIRINMAL_KEYTOOL%" -genkeypair -v -storetype JKS -keystore "%KIRINMAL_JKS%" -alias "%KIRINMAL_ALIAS%" -dname "CN=Lanz Magick, OU=KirinMaL, O=KirinMaL" -keyalg RSA -keysize 4096 -validity 10000
if errorlevel 1 goto generation_failed

rem Paths use environment variables, without interpolating them into PowerShell.
powershell.exe -NoLogo -NoProfile -NonInteractive -Command "$ErrorActionPreference='Stop'; [IO.File]::WriteAllText($env:KIRINMAL_BASE64, [Convert]::ToBase64String([IO.File]::ReadAllBytes($env:KIRINMAL_JKS)), [Text.Encoding]::ASCII)"
if errorlevel 1 goto export_failed
> "%KIRINMAL_OUTPUT%\KEY_ALIAS.txt" echo %KIRINMAL_ALIAS%
(
    echo GitHub: lanzmagick/KirinMaL
    echo Settings - Secrets and variables - Actions
    echo.
    echo KEYSTORE_FILE: salin semua kandungan KEYSTORE_FILE.txt.
    echo KEYSTORE_PASSWORD: password keystore yang kau isi tadi.
    echo KEY_ALIAS: %KIRINMAL_ALIAS%
    echo KEY_PASSWORD: password key yang kau isi tadi.
    echo Jika kau tekan Enter pada password key, guna password keystore yang sama.
    echo.
    echo MAL_CLIENT_ID tidak diubah oleh skrip ini.
    echo Simpan JKS dan password sendiri. Jangan commit fail key atau base64 ke repo.
) > "%KIRINMAL_OUTPUT%\CARA-ISI-SECRETS.txt"
echo.
echo SIAP. Fail disimpan dalam:
echo "%KIRINMAL_OUTPUT%"
echo Buka CARA-ISI-SECRETS.txt untuk empat nilai GitHub Secrets.
echo Password tidak disimpan oleh skrip ini. Simpan password yang kau pilih.
pause
exit /b 0

:need_java
echo GAGAL: keytool tidak dijumpai. Pasang Java JDK dan buka terminal semula.
goto failed
:need_powershell
echo GAGAL: Windows PowerShell diperlukan untuk menghasilkan base64.
goto failed
:already_exists
echo Fail KirinMaL-release.jks sudah ada dalam folder output.
echo Key itu tidak ditimpa. Untuk key lain, letak CMD ini dalam folder kosong lain.
goto failed
:output_failed
echo GAGAL: folder output tidak dapat dicipta. Letak CMD dalam folder yang boleh ditulis.
goto failed
:generation_failed
echo GAGAL: keytool tidak berjaya menjana key. Semak mesej keytool di atas.
goto failed
:export_failed
echo Key JKS sudah dicipta, tetapi eksport base64 gagal. Simpan fail JKS itu.
goto failed
:failed
pause
exit /b 1
