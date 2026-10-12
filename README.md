# <img src="assets/branding/kirinmal-wordmark.svg" width="480" alt="KirinMaL" />

**KirinMaL** is an unofficial [MyAnimeList](https://myanimelist.net/) client based on [MoeList Multiplatform](https://github.com/axiel7/MoeListKMP), customized for Android. The repository also includes iOS and desktop source code.

## Features

- Track anime and manga with your MyAnimeList account.
- Anime Menu and Manga Menu: MAL rankings plus one bento Genres tab with full MAL category names and AniList discovery. Only MAL-linked titles open the MAL editor, so tracking remains in MyAnimeList.
- Seasonal anime, calendar and recommendations, with a pinned Home search bar.
- Anime & Manga Stats inspired by KirinNime, including progress and reading/watching totals.
- Optional NSFW visibility (enabled by default for new installs; switch it off in Settings).
- KirinMaL icons and branding across platforms.

## Website

**[KirinMaL Official Pages](https://lanzmagick.github.io/KirinMaL/)** — lightweight, responsive landing page with features and Android APK download links.

## Download

**[Get the latest Development APK](https://github.com/lanzmagick/KirinMaL/releases/tag/development)** · [All Releases](https://github.com/lanzmagick/KirinMaL/releases)

Development APKs are test builds; device installation and MAL login are not yet fully verified. Builds are published to **GitHub Releases**, not Actions artifacts.

## Build (Android)

Requires **JDK 21** and the Android SDK. Create `private.properties` at the repository root:

```properties
CLIENT_SECRET=""
```

Then run:

```bash
./gradlew :app:androidApp:assembleDebug
```

See [GitHub Actions](https://github.com/lanzmagick/KirinMaL/actions) for build results. Internal package identifiers remain unchanged to preserve MAL OAuth compatibility.

## Credits & License

Based on [axiel7/MoeListKMP](https://github.com/axiel7/MoeListKMP), with credit to its original authors and contributors. KirinMaL is **not affiliated with MyAnimeList**.

Licensed under [GPL-3.0](LICENSE).
