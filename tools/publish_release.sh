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

# A superseded workflow must never overwrite the latest rolling APK.
if [ "$tag" = "development" ]; then
  latest="$(gh api "repos/$repo/git/ref/heads/main" --jq '.object.sha')"
  if [ "$latest" != "$revision" ]; then
    echo "Skipping obsolete build $revision (current main: $latest)"
    exit 0
  fi
fi
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
      echo "Debug APK publication is disabled; only signed release APKs are accepted." >&2
      exit 2
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
  echo "## Latest 5 changes"
  echo
  # Show a maximum of five recent changes so release notes stay concise.
  git log -n 5 --format='- %h %s (%cs)' || true
} > "$notes"

# Keep curated notes for stable versions, not just the latest five commits.
if [ "$tag" != "development" ] && [ -f "docs/releases/$tag.md" ]; then
  cp "docs/releases/$tag.md" "$notes"
fi

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
    gh release create "$tag" --target "$revision" --repo "$repo" --title "$title" --draft --notes-file "$notes"
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
  gh release edit "$tag" --repo "$repo" --draft=false --prerelease=false --notes-file "$notes"
fi
echo "Published to https://github.com/$repo/releases/tag/$tag"
