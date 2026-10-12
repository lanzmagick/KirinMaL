# Own MyAnimeList OAuth Client ID for KirinMaL Android

KirinMaL Android uses **your MyAnimeList API application Client ID**, not the original MoeList Client ID.

## Setup

1. Log in to MyAnimeList and open the API application registration page: https://myanimelist.net/apiconfig .
2. Create a new application for KirinMaL.
3. Set **App Redirect URL** to exactly: `kirinmal://auth/`.
4. Copy the application's **Client ID** (not Client Secret).
5. Open **GitHub → KirinMaL → Settings → Secrets and variables → Actions → New repository secret**.
6. Name it `MAL_CLIENT_ID` and paste just the Client ID value.
7. Run **Actions → KirinMaL Signed Release → Run workflow** after the next version bump.

The Android build reads `MAL_CLIENT_ID` from its CI environment. For local builds, the same property may be put in `private.properties` (gitignored):

```properties
CLIENT_SECRET=""
MAL_CLIENT_ID=replace-with-your-own-mal-client-id
```

The Android OAuth redirect is **KirinMaL's own** `kirinmal://auth/`, and it **must match exactly** the URL registered in your MyAnimeList API application. Update or register your MAL API application with this new redirect URL before using the next signed APK. The app's package ID is `com.lanzmagick.kirinmal`. No original MoeList Client ID is used by the Android build.

The Client ID is a public identifier (embedded in Android APKs); keep any **Client Secret** and Android signing credentials private. The signed-release CI fails safely if `MAL_CLIENT_ID` is missing. Verification builds use a dummy ID only to compile; their APKs are not distributed.

Changing the Client ID may require logging out and signing in again because OAuth tokens belong to the original client registration.
