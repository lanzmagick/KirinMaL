#!/usr/bin/env bash
# Audit actual signed APKs before publishing. Do not upload private signing material.
set -euo pipefail
if [ "$#" -lt 1 ]; then echo "Usage: bash tools/audit_android_release.sh FILE.apk [...]" >&2; exit 2; fi
test -n "$ANDROID_HOME" || { echo "::error::ANDROID_HOME is missing"; exit 1; }
aapt="$(find "$ANDROID_HOME/build-tools" -maxdepth 2 -name aapt -type f | sort -V | tail -n 1)"
signer="$(find "$ANDROID_HOME/build-tools" -maxdepth 2 -name apksigner -type f | sort -V | tail -n 1)"
test -x "$aapt" && test -x "$signer" || { echo "::error::SDK signing tools unavailable"; exit 1; }
first_cert=""
for apk in "$@"; do
  test -s "$apk" || { echo "::error::Missing APK: $apk"; exit 1; }
  unzip -tqq "$apk" || { echo "::error::Malformed ZIP/APK: $apk"; exit 1; }
  info="$("$aapt" dump badging "$apk")"
  grep -Fq "package: name='com.lanzmagick.kirinmal'" <<< "$info" || { echo "::error::Unexpected Android package"; exit 1; }
  if grep -Fq "application-debuggable" <<< "$info"; then echo "::error::Debuggable APK"; exit 1; fi
  target="$(grep -E "^targetSdkVersion:" <<< "$info" | head -n 1 | grep -oE '[0-9]+' | head -n 1 || true)"
  test -n "$target" && test "$target" -ge 33 || { echo "::error::Outdated target SDK"; exit 1; }

  permissions="$("$aapt" dump permissions "$apk")"
  echo "Merged APK permissions:"
  printf '%s\n' "$permissions"
  if grep -E 'android\.permission\.(READ_SMS|RECEIVE_SMS|SEND_SMS|READ_CALL_LOG|READ_CONTACTS|QUERY_ALL_PACKAGES|REQUEST_INSTALL_PACKAGES|MANAGE_EXTERNAL_STORAGE|SYSTEM_ALERT_WINDOW|BIND_ACCESSIBILITY_SERVICE|BIND_NOTIFICATION_LISTENER_SERVICE)' <<< "$permissions"; then
    echo "::error::Unexpected sensitive permission in release. Review dependencies."
    exit 1
  fi
  "$signer" verify --verbose --print-certs "$apk" > "$RUNNER_TEMP/release-signer.txt"
  grep -Fq 'Verified using v2 scheme (APK Signature Scheme v2): true' "$RUNNER_TEMP/release-signer.txt" || { echo "::error::Missing v2 signature"; exit 1; }
  cert="$(sed -n 's/^Signer #1 certificate SHA-256 digest: //p' "$RUNNER_TEMP/release-signer.txt" | head -n 1)"
  test -n "$cert" || { echo "::error::Signing certificate not found"; exit 1; }
  if [ -n "$first_cert" ] && [ "$first_cert" != "$cert" ]; then echo "::error::Inconsistent certificate"; exit 1; fi
  first_cert="$cert"
  printf 'Verified %s | SDK %s | Certificate %s\n' "$(basename "$apk")" "$target" "$cert"
  sha256sum "$apk"
done
echo "Local release-security audit passed; Google Play Protect classification is independent."
