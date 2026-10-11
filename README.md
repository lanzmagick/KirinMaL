# <img src="assets/branding/kirinmal-wordmark.svg" width="540" alt="KirinMaL" />

KirinMaL is a customized fork of [MoeList Multiplatform](https://github.com/axiel7/MoeListKMP), an unofficial [MyAnimeList](https://myanimelist.net/) client. The project contains Android, iOS and desktop code; Android is the current focus of this fork.

> **Status:** Work in progress. Source version: `4.1.1` (code `406`). This does not imply that a tested KirinMaL APK has been published.

## Current progress

Updated **12 October 2026**:

- [x] Preserve the existing MoeList-based multiplatform project and MAL integration.
- [x] Correct the manga progress start-date condition (chapter and volume modes).
- [x] Scope anime/manga tab models by media type, avoiding accidental state reuse.
- [x] Display **KirinMaL** as the Android app name.
- [x] Verify Android debug compilation with GitHub Actions; app login still requires device testing.
- [ ] Review independent Android package ID and MAL OAuth redirect **together** before changing either one.
- [x] Update Android, desktop and iOS display names, downloadable artifact names, and GitHub links.
- [x] Add original KirinMaL kirin icon artwork and automated generation for Android, iOS and desktop launchers.
- [x] Generate and commit Android, iOS and desktop launcher images with GitHub Actions.
- [ ] Verify the Android APK build and login after applying the new icons.
- [x] Pin the Home search bar while the Home feed scrolls (keep collapsing behavior on other tabs).
- [x] Add four functional Home quick-access cards: Search Anime, Search Manga, Recommendations and Profile (two columns on phone, four on desktop).
- [x] Restore live MAL manga statistics on Profile using KirinNime's Jikan approach, with a website-style status bar, reading counts and retry for API errors.
- [ ] Verify anime/manga list editing, paging, search and navigation on device.

The internal Kotlin packages and Android application ID are still inherited from MoeList for compatibility. Do not change only one part of the MAL OAuth configuration.

The original internal source directories and package identifiers remain unchanged to protect compatibility with upstream components and MAL authentication.

## MAL Manga Stats

The manga statistics section in Profile no longer shows the inherited "Jikan shut down" warning. It uses the existing Jikan user statistics endpoint, following the logic in KirinNime. When the API provides manga data, KirinMaL displays Reading, Completed, On Hold, Dropped, Plan to Read, Days Read, Mean Score, Total Entries, Rereads, Chapters and Volumes in a MAL website-inspired layout. API failures display a retry option instead of fabricated zero totals.

## Home quick access

The original Anime Ranking, Manga Ranking, Seasonal Chart and Calendar cards remain. A second Quick Access group adds working shortcuts for anime search, manga search, recommendations and the MAL profile. On compact screens the four new cards occupy two rows; on large screens they occupy one row. The shared Home search bar stays pinned during feed scrolling.

Planned next (not yet implemented): Continue Watching/Reading, Recently Viewed, optional NSFW-genre discovery shortcuts and customization of Home card order.

## Adult-content visibility

The existing **Show NSFW content** setting (Settings → Content) is **enabled by default** for new installs. MyAnimeList's `nsfw=1` API option is applied to anime and manga searches, lists and rankings, as well as anime seasons and recommendations. This permits titles labelled Hentai, Erotica, Ecchi and other adult categories when MyAnimeList makes them available. The user can still switch it off; a previously saved choice to hide NSFW content is preserved. Changing the setting refreshes list/search/ranking feeds without needing to reinstall the app.

## Build Android

Use JDK 21 and a configured Android SDK. Create `private.properties` in the repository root:

```properties
CLIENT_SECRET=""
```

The client secret is required for the desktop variant, not the Android variant (see the upstream project documentation). To build a debug APK:

```bash
./gradlew :app:androidApp:assembleDebug
```

See [GitHub Actions](https://github.com/lanzmagick/KirinMaL/actions) for repository workflows. An Android debug build has passed CI; installation and MAL login on a device have not yet been verified.

## Download builds from GitHub Releases

KirinMaL does **not** store downloadable APKs and desktop packages in GitHub Actions artifacts. Download builds from [GitHub Releases](https://github.com/lanzmagick/KirinMaL/releases) instead.

- **Development** (rolling pre-release at tag `development`): updated Android debug APK and manually built desktop packages replace previous files of the same type. It includes build commit, UTC date and the last 30 days of commit notes. Debug APKs are for testing.
- **Versioned releases** (tags like `v4.1.1`): signed Android release APKs are published when the Android release workflow completes with the required signing secrets.

For CI logs, use [GitHub Actions](https://github.com/lanzmagick/KirinMaL/actions), but binaries belong in Releases.

## Upstream credits and license

KirinMaL is based on [axiel7/MoeListKMP](https://github.com/axiel7/MoeListKMP). Credit for the original app, architecture, translation work and features belongs to the original authors and contributors. This is **not** an official MyAnimeList app.

The original repository contains additional project documentation, installation information and links to support its developer. See [the upstream README](https://github.com/axiel7/MoeListKMP#readme).

Distributed under the terms of the [GNU General Public License v3.0](LICENSE).

## KirinMaL artwork

![KirinMaL icon](assets/branding/kirinmal-icon.webp)

The scalable `kirinmal-wordmark.svg` is used in this README.

Launcher icons are regenerated from this artwork by `tools/generate_brand_assets.py` and the `Apply KirinMaL App Icons` workflow. The artwork is a small, optimized source suitable for icons; the original high-resolution generated image is available in the development conversation.
