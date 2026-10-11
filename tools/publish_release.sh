#!/usr/bin/env bash
# Publish KirinMaL build outputs directly to GitHub Releases (not Actions artifacts).
# Usage: bash tools/publish_release.sh TAG CHANNEL FILE...
# TAG=development is a rolling pre-release; vX.Y.Z tags are stable version releases.
set -euo pipefail

if [ "$#" -lt 3 ]; then
  echo "Usage: $0 TAG CHANNEL FILE..." >&2
  exit 2
fi

tag="$1"
channel="$2"
shift 2
repo="$GITHUB_REPOSITORY"
revision="$GITHUB_SHA"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
index=0

for src in "$@"; do
  if [ ! -f "$src" ]; then
    echo "Missing build output: $src" >&2
    exit 1
  fi
  index=$((index + 1))
  base="$(basename "$src")"
  case "$channel" in
    android-debug)
      case "$base" in
        *arm64-v8a*) asset="KirinMaL-debug-arm64-v8a.apk" ;;
        *armeabi-v7a*) asset="KirinMaL-debug-armeabi-v7a.apk" ;;
        *x86_64*) asset="KirinMaL-debug-x86_64.apk" ;;
        *x86*) asset="KirinMaL-debug-x86.apk" ;;
        *) asset="KirinMaL-debug-universal.apk" ;;
      esac
      ;;
    windows)
      asset="KirinMaL-windows-$index.msi"
      ;;
    linux)
      asset="KirinMaL-linux-$index.$(printf '%s' "$base" | awk -F. '{print $NF}')"
      ;;
    appimage)
      asset="KirinMaL-AppImage-$index.AppImage"
      ;;
    android-release)
      asset="$base"
      ;;
    *)
      echo "Unsupported release channel: $channel" >&2
      exit 2
      ;;
  esac
  cp "$src" "$tmp/$asset"
  echo "Release asset: $asset"
done

notes="$tmp/release-notes.md"
{
  echo "## KirinMaL build"
  echo
  echo "- Channel: $channel"
  echo "- Commit: [$revision](https://github.com/$repo/commit/$revision)"
  echo "- Built: $(date -u '+%Y-%m-%d %H:%M UTC')"
  echo
  if [ "$tag" = "development" ]; then
    echo "This is a rolling **pre-release**. Updated builds replace older files with the same name. Debug APKs are not signed as production releases."
  else
    echo "Versioned release built from the KirinMaL source. Android release APKs use the repository's signing configuration."
  fi
  echo
  echo "## Changes in the last 30 days"
  echo
  git log --since='30 days ago' -n 100 --format='- %h %s (%cs)' || true
} > "$notes"

set -- "$tmp"/*
# The release notes are for GitHub, not downloadable assets.
for file in "$@"; do
  if [ "$file" = "$notes" ]; then
    continue
  fi
  gh release view "$tag" --repo "$repo" >/dev/null 2>&1 && break
  title="KirinMaL $tag"
  if [ "$tag" = "development" ]; then
    gh release create "$tag" --target "$revision" --repo "$repo" --title "KirinMaL Development" --prerelease --notes-file "$notes"
  else
    gh release create "$tag" --target "$revision" --repo "$repo" --title "$title" --notes-file "$notes"
  fi
  break
done

# Upload one at a time; --clobber replaces the previously published development build.
for file in "$tmp"/*; do
  [ "$file" = "$notes" ] && continue
  gh release upload "$tag" "$file" --clobber --repo "$repo"
done
if [ "$tag" = "development" ]; then
  gh release edit "$tag" --repo "$repo" --title "KirinMaL Development" --prerelease --notes-file "$notes"
else
  gh release edit "$tag" --repo "$repo" --notes-file "$notes"
fi
echo "Published to https://github.com/$repo/releases/tag/$tag"
