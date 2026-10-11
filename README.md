# KirinMaL

KirinMaL is a customized fork of [MoeList Multiplatform](https://github.com/axiel7/MoeListKMP), an unofficial [MyAnimeList](https://myanimelist.net/) client. The project contains Android, iOS and desktop code; Android is the current focus of this fork.

> **Status:** Work in progress. Source version: `4.1.1` (code `406`). This does not imply that a tested KirinMaL APK has been published.

## Current progress

Updated **11 October 2026**:

- [x] Preserve the existing MoeList-based multiplatform project and MAL integration.
- [x] Correct the manga progress start-date condition (chapter and volume modes).
- [x] Scope anime/manga tab models by media type, avoiding accidental state reuse.
- [x] Display **KirinMaL** as the Android app name.
- [ ] Verify an Android debug/release build and login (Android SDK setup failure addressed; compilation still needs verification).
- [ ] Review independent Android package ID and MAL OAuth redirect **together** before changing either one.
- [x] Update Android, desktop and iOS display names, downloadable artifact names, and GitHub links.
- [ ] Replace inherited MoeList app artwork/icons with KirinMaL artwork.
- [ ] Verify anime/manga list editing, paging, search and navigation on device.

The internal Kotlin packages and Android application ID are still inherited from MoeList for compatibility. Do not change only one part of the MAL OAuth configuration.

The original internal source directories and package identifiers remain unchanged to protect compatibility with upstream components and MAL authentication.

## Build Android

Use JDK 21 and a configured Android SDK. Create `private.properties` in the repository root:

```properties
CLIENT_SECRET=""
```

The client secret is required for the desktop variant, not the Android variant (see the upstream project documentation). To build a debug APK:

```bash
./gradlew :app:androidApp:assembleDebug
```

See [GitHub Actions](https://github.com/lanzmagick/KirinMaL/actions) for repository workflows. A successful build is not yet verified for the current fork.

## Upstream credits and license

KirinMaL is based on [axiel7/MoeListKMP](https://github.com/axiel7/MoeListKMP). Credit for the original app, architecture, translation work and features belongs to the original authors and contributors. This is **not** an official MyAnimeList app.

The original repository contains additional project documentation, installation information and links to support its developer. See [the upstream README](https://github.com/axiel7/MoeListKMP#readme).

Distributed under the terms of the [GNU General Public License v3.0](LICENSE).
