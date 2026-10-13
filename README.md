# <img src="assets/branding/kirinmal-wordmark.svg" width="480" alt="KirinMaL" />

**KirinMaL** is an unofficial [MyAnimeList](https://myanimelist.net/) client based on [MoeList Multiplatform](https://github.com/axiel7/MoeListKMP), customized for Android. The repository also includes iOS and desktop source code.

**Android identity:** `com.lanzmagick.kirinmal` · custom MAL redirect `kirinmal://oauth/mal` · owner-supplied `MAL_CLIENT_ID` GitHub Action secret. Login requires registering that exact redirect in the owner's MAL API application. The original project remains credited as the upstream source.

## Features

- Track anime and manga with your MyAnimeList account.
- Anime and Manga list controls: title-initial filter for A–Z, 1–0 and symbols beside Sort, with MAL status colors on the status badges and floating selector.
- Random and Magic for both anime and manga lists: keep the default list picker, or reveal a surprise in a popup inspired by Netflix Kids' Mystery Box. Magic stays visible when the Random button is disabled in Settings. Home offers Magic Anime and Magic Manga in an equal-size shortcut bento.
- Home personal carousels: Recent Watching Anime with episode progress, Reading Manga with chapter/volume progress, and Recommended Manga based on the user's Reading or Completed titles. Each section opens its matching list.
- Anime Menu and Manga Menu: MAL rankings plus one bento Genres tab with full MAL category names and AniList discovery. Only MAL-linked titles open the MAL editor, so tracking remains in MyAnimeList.
- Seasonal anime, calendar and recommendations, with a pinned Home search bar.
- Anime & Manga Stats inspired by KirinNime, including progress and reading/watching totals.
- Optional NSFW visibility (enabled by default for new installs; switch it off in Settings).
- KirinMaL icons and branding across platforms.
- Optional About menu in More, hidden by default and enabled under Settings → Display → Show About menu. The About page and upstream credits remain intact.

## Website

**[KirinMaL Website](https://lanzmagick.github.io/KirinMaL/)** — responsive, screenshot-free landing page with KirinMaL branding, features and direct APK download links.

## Download

**[KirinMaL Stable Releases](https://github.com/lanzmagick/KirinMaL/releases)**

**Latest published signed APK: [v1.0.4](https://github.com/lanzmagick/KirinMaL/releases/tag/v1.0.4).** It adds title-initial filtering beside Sort, MAL-colored status badges, three manga carousels on Home, and Magic surprise pickers for anime and manga. Shuffle-bag tests, Android compilation and release-signature/manifest audits passed before publication. See [v1.0.4 release notes](docs/releases/v1.0.4.md).

Direct APK downloads: [ARM64](https://github.com/lanzmagick/KirinMaL/releases/download/v1.0.4/KirinMaL-1.0.4-arm64-v8a-release.apk) · [Universal](https://github.com/lanzmagick/KirinMaL/releases/download/v1.0.4/KirinMaL-1.0.4-universal-release.apk). Other ABI variants are available on the release page.

**Pending patches on main:** Sort : All / Sort : A labels, the replacement Home carousels, the refreshed MAL login button, a Home bento with only Magic Anime / Magic Manga, list Magic independent of the Random toggle, and an optional About menu await the next Android build. They are not included in the published v1.0.4 APK. See [unreleased changes](docs/UNRELEASED.md).

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

- **Anime and Manga lists:** Magic sits immediately beside the existing Random button. The toolbar order is Sort → title-initial filter → Random → Magic, with horizontal scrolling on narrow screens. The existing Settings toggle controls only Random; disabling it leaves Sort → title-initial filter → Magic. The default Random behavior and saved preference are preserved. Magic reads every MAL page of the selected list status, then applies the current A–Z, 1–0 or symbols filter using the user's preferred title language.
- **Home:** Magic Anime and Magic Manga share the shortcut bento with Anime Menu, Manga Menu, Seasonal Chart and Calendar. The six cards have equal widths and an 84 dp height, with two columns on compact screens and three on larger screens. Home's two direct Random buttons have been removed. Each Magic type requests the first 100 Popularity results and the first 100 Score results, then merges them by MAL ID. This is a discovery sample of up to 200 unique titles, not the entire MAL catalog. Magic reveals the chosen title in the popup first.
- **Selection:** invalid IDs and blank titles are removed. A shuffled bag gives each unique candidate one turn before refilling. With two or more candidates, it avoids choosing the same title immediately at refill or after reloading; a one-title pool necessarily repeats.
- **Freshness:** opening a picker reloads its MAL candidates. Within the popup, **Surprise me again** reuses the bag; changing title language or NSFW visibility causes a reload on the next pick. The repositories apply the user's NSFW preference.
- **Empty lists and failures:** an empty result or failed request shows a message and allows another attempt. Closing the popup cancels its pending request. Choosing a surprise only opens its Anime/Manga Info page; list edits use the existing MAL editor.

Shuffle-bag tests cover unique turns, duplicate IDs, refill repeats, pool replacement, and empty/single-title pools. The signed release workflow runs these tests before building and auditing the APKs.

## Home personal discovery

Home's Recommended Manga rail merges MAL title recommendations from up to three recently updated Reading manga, falling back to Completed manga when Reading is empty. It sums recommendation votes, removes duplicate IDs and titles already in any of the user's manga lists, and respects NSFW visibility. Home shows up to 25 results; its full grid shows up to 100. Recent Watching Anime reads the user's Watching list in updated order. These personal sections refresh after Quick Edit and clear on logout.

## KirinMaL MyAnimeList Client ID (Android)

KirinMaL Android no longer embeds the original MoeList OAuth Client ID. Register your **own MAL API application**, using the exact redirect URI `kirinmal://oauth/mal`. Add its **Client ID** (not the client secret) to GitHub Actions repository secret **`MAL_CLIENT_ID`** before publishing a signed APK. The release workflow refuses to publish without the owner's ID. For local builds add `MAL_CLIENT_ID=your-client-id` to the gitignored `private.properties`.

See [MAL Client ID instructions](docs/MAL_CLIENT_ID.md). KirinNime uses the separate redirect `kirinime://oauth/mal`; register KirinMaL with its own redirect.

The Anime and Manga Genres pages now reserve extra bottom spacing above Android's system navigation bar so the final Explicit category can be scrolled fully into view.

See [Release signing instructions](docs/ANDROID_RELEASE.md) and [Play Protect diagnosis](docs/PLAY_PROTECT.md). The internal Kotlin namespace remains `com.axiel7.moelist`; Android identifies the installed app by `com.lanzmagick.kirinmal`. Installing this separate package does not inherit local data from MoeList or KirinMaL v1.0.0. Changing the MAL Client ID may require signing in again.

## Credits & License

Based on [axiel7/MoeListKMP](https://github.com/axiel7/MoeListKMP), with credit to its original authors and contributors. KirinMaL is **not affiliated with MyAnimeList**.

The Magic popup's surprise-box interaction is inspired by **Netflix Kids' Mystery Box**, credited [above](#magic-discovery-inspiration-and-picker-logic). KirinMaL is not affiliated with Netflix.

Licensed under [GPL-3.0](LICENSE).
