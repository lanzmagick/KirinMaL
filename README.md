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

**[KirinMaL Website](https://lanzmagick.github.io/KirinMaL/)** — responsive, screenshot-free landing page with KirinMaL branding, features and direct APK download links.

## Download

**[KirinMaL Stable Releases](https://github.com/lanzmagick/KirinMaL/releases)**

**v1.0.3** keeps the KirinMaL Android application ID (`com.lanzmagick.kirinmal`) and adds checks of the merged APK permissions, signing certificate, package identity, target SDK and archive integrity. It installs separately from MoeList (`com.axiel7.moelist`) and earlier KirinMaL releases. No debug APKs are published; signed APKs are distributed through GitHub Releases.

## Build (Android)

Requires **JDK 21** and the Android SDK. Create `private.properties` at the repository root:

```properties
CLIENT_SECRET=""
```

To publish **signed** APKs, set the four GitHub Actions repository secrets `KEYSTORE_FILE` (base64-encoded keystore), `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`. Then use **Actions → KirinMaL Signed Release → Run workflow**. Keep the keystore safely offline for future updates; do not commit secrets.

## KirinMaL MyAnimeList Client ID (Android)

KirinMaL Android no longer embeds the original MoeList OAuth Client ID. Register your **own MAL API application**, using the exact redirect URI `moelist://moelist.page.link/`. Add its **Client ID** (not the client secret) to GitHub Actions repository secret **`MAL_CLIENT_ID`** before publishing a signed APK. The release workflow refuses to publish without the owner's ID. For local builds add `MAL_CLIENT_ID=your-client-id` to the gitignored `private.properties`.

The Anime and Manga Genres pages now reserve extra bottom spacing above Android's system navigation bar so the final Explicit category can be scrolled fully into view.

See [Release signing instructions](docs/ANDROID_RELEASE.md) and [Play Protect diagnosis](docs/PLAY_PROTECT.md). The **Android application ID is KirinMaL-specific**. The internal Kotlin namespace and MyAnimeList OAuth redirect URI (`moelist://moelist.page.link/`) remain unchanged for compatibility. After installing the new KirinMaL package, log into MAL again; it does not inherit the old app’s local data.

## Credits & License

Based on [axiel7/MoeListKMP](https://github.com/axiel7/MoeListKMP), with credit to its original authors and contributors. KirinMaL is **not affiliated with MyAnimeList**.

Licensed under [GPL-3.0](LICENSE).
