# <img src="assets/branding/kirinmal-wordmark.svg" width="480" alt="KirinMaL" />

**KirinMaL** is an unofficial [MyAnimeList](https://myanimelist.net/) client based on [MoeList Multiplatform](https://github.com/axiel7/MoeListKMP), customized for Android. The repository also includes iOS and desktop source code.

**Android identity:** `com.lanzmagick.kirinmal` · custom MAL redirect `kirinmal://oauth/mal` · owner-supplied `MAL_CLIENT_ID` GitHub Action secret. Login requires registering that exact redirect in the owner's MAL API application. The original project remains credited as the upstream source.

## Features

- Track anime and manga with your MyAnimeList account.
- Anime and Manga list controls: title-initial filter for A–Z, 1–0 and symbols beside Sort, with MAL status colors on the status badges and floating selector.
- Random and Magic for both anime and manga: keep the default list picker, or reveal a surprise in a popup inspired by Netflix Kids' Mystery Box. Home has separate Anime and Manga controls.
- Home manga carousels: Reading Manga with chapter/volume progress, Popular Manga by MAL Members, and Top Rated Manga by MAL score. Each section opens its matching full list.
- Anime Menu and Manga Menu: MAL rankings plus one bento Genres tab with full MAL category names and AniList discovery. Only MAL-linked titles open the MAL editor, so tracking remains in MyAnimeList.
- Seasonal anime, calendar and recommendations, with a pinned Home search bar.
- Anime & Manga Stats inspired by KirinNime, including progress and reading/watching totals.
- Optional NSFW visibility (enabled by default for new installs; switch it off in Settings).
- KirinMaL icons and branding across platforms.

## Website

**[KirinMaL Website](https://lanzmagick.github.io/KirinMaL/)** — responsive, screenshot-free landing page with KirinMaL branding, features and direct APK download links.

## Download

**[KirinMaL Stable Releases](https://github.com/lanzmagick/KirinMaL/releases)**

**Latest published signed APK: v1.0.3.** It adds safe bottom spacing to Anime and Manga Genres, an owner-supplied MAL Client ID and the KirinMaL redirect `kirinmal://oauth/mal`.

The next version, **v1.0.4**, adds title-initial filtering beside Sort, MAL-colored status badges, three manga carousels on Home, and Magic surprise pickers for anime and manga. Its patch is ready; the Android build has not been run yet. See [v1.0.4 release notes](docs/releases/v1.0.4.md).

The Android application ID is `com.lanzmagick.kirinmal`, separate from MoeList and KirinMaL v1.0.0 (`com.axiel7.moelist`). Versions v1.0.1 and later share the KirinMaL package ID and update the same installation when signed with the same release key. Signed APKs are distributed through GitHub Releases. See [v1.0.3 release notes](docs/releases/v1.0.3.md).

## Build (Android)

Requires **JDK 21** and the Android SDK. Create `private.properties` at the repository root:

```properties
CLIENT_SECRET=""
MAL_CLIENT_ID=your-own-mal-client-id
```

To publish **signed** APKs, set the four GitHub Actions signing secrets `KEYSTORE_FILE` (base64-encoded keystore), `KEYSTORE_PASSWORD`, `KEY_ALIAS` and `KEY_PASSWORD`, plus your own **`MAL_CLIENT_ID`**. Then use **Actions → KirinMaL Signed Release → Run workflow → main**. Keep the keystore safely offline for future updates; do not commit signing credentials.

## Magic discovery: inspiration and picker logic

**UI inspiration: Netflix Kids' [Mystery Box](https://about.netflix.com/en/news/surprise-delight-and-discover-your-next-favorite-with-the-new-kids-mystery).** Credit to Netflix for the sparkling box and surprise-reveal concept. KirinMaL's popup uses its own gradient, vector sparkles and animation, then shows one title with **Open info** and **Surprise me again**. This is an independent implementation; it does not use Netflix's assets or recommendation engine.

- **Anime and Manga lists:** Magic sits immediately beside the existing Random button. The toolbar order is Sort → title-initial filter → Random → Magic, with horizontal scrolling on narrow screens. The default Random behavior is preserved. Magic reads every MAL page of the selected list status, then applies the current A–Z, 1–0 or symbols filter using the user's preferred title language.
- **Home:** separate Random Anime / Magic Anime and Random Manga / Magic Manga buttons use real titles returned by MAL. Each type requests the first 100 Popularity results and the first 100 Score results, then merges them by MAL ID. This is a discovery sample of up to 200 unique titles, not the entire MAL catalog. Default Random opens the chosen title directly; Magic reveals it in the popup first.
- **Selection:** invalid IDs and blank titles are removed. A shuffled bag gives each unique candidate one turn before refilling. With two or more candidates, it avoids choosing the same title immediately at refill or after reloading; a one-title pool necessarily repeats.
- **Freshness:** opening a picker reloads its MAL candidates. Within the popup, **Surprise me again** reuses the bag; changing title language or NSFW visibility causes a reload on the next pick. The repositories apply the user's NSFW preference.
- **Empty lists and failures:** an empty result or failed request shows a message and allows another attempt. Closing the popup cancels its pending request. Choosing a surprise only opens its Anime/Manga Info page; list edits use the existing MAL editor.

Shuffle-bag tests cover unique turns, duplicate IDs, refill repeats, pool replacement, and empty/single-title pools. They are included in the patch; the Kotlin tests and Android build will be run when the build is requested.

## KirinMaL MyAnimeList Client ID (Android)

KirinMaL Android no longer embeds the original MoeList OAuth Client ID. Register your **own MAL API application**, using the exact redirect URI `kirinmal://oauth/mal`. Add its **Client ID** (not the client secret) to GitHub Actions repository secret **`MAL_CLIENT_ID`** before publishing a signed APK. The release workflow refuses to publish without the owner's ID. For local builds add `MAL_CLIENT_ID=your-client-id` to the gitignored `private.properties`.

See [MAL Client ID instructions](docs/MAL_CLIENT_ID.md). KirinNime uses the separate redirect `kirinime://oauth/mal`; register KirinMaL with its own redirect.

The Anime and Manga Genres pages now reserve extra bottom spacing above Android's system navigation bar so the final Explicit category can be scrolled fully into view.

See [Release signing instructions](docs/ANDROID_RELEASE.md) and [Play Protect diagnosis](docs/PLAY_PROTECT.md). The internal Kotlin namespace remains `com.axiel7.moelist`; Android identifies the installed app by `com.lanzmagick.kirinmal`. Installing this separate package does not inherit local data from MoeList or KirinMaL v1.0.0. Changing the MAL Client ID may require signing in again.

## Credits & License

Based on [axiel7/MoeListKMP](https://github.com/axiel7/MoeListKMP), with credit to its original authors and contributors. KirinMaL is **not affiliated with MyAnimeList**.

The Magic popup's surprise-box interaction is inspired by **Netflix Kids' Mystery Box**, credited [above](#magic-discovery-inspiration-and-picker-logic). KirinMaL is not affiliated with Netflix.

Licensed under [GPL-3.0](LICENSE).
