# KirinMaL — Android Signed Releases

The first public signed release was v1.0.0. **v1.0.1** introduces a distinct Android application ID; **v1.0.2** audits the signed APK before publication. Builds are distributed through **GitHub Releases**, never as debug APKs or Actions artifacts.

## Four GitHub Actions secrets

The owner must add these values under **Settings → Secrets and variables → Actions → New repository secret**:

1. `KEYSTORE_FILE` — full base64 of the private `.jks` signing keystore, on a single line.
2. `KEYSTORE_PASSWORD` — password to unlock the keystore.
3. `KEY_ALIAS` — alias of the signing key inside the keystore.
4. `KEY_PASSWORD` — password to unlock the signing key.

These values come from the owner's privately provided **KirinMaL-4-Kunci-Release.zip** file and **must never be committed to the repository** or published as an artifact.

## Publish stable APKs

1. Back up the private JKS file and passwords. If the keystore is lost, future Android updates cannot use the same signature.
2. Set all four secrets.
3. Go to **Actions → KirinMaL Signed Release → Run workflow → main**.
4. Wait for **Build signed production APKs**, **Verify every APK has an Android release signature**, and **Publish KirinMaL first stable release** to succeed.
5. Open **Releases → v1.0.1** and download the matching device APK. The draft only becomes public **after** signed APKs have been uploaded and verified.
6. The old `development` release containing debug APKs is removed during first-release preparation or successful signed publication.

The `Verify KirinMaL Android Release` workflow compiles an **unsigned test release** on pushes to check Kotlin/Android compilation; it **never publishes APKs**.

**Android identity:** From v1.0.1 onwards the release `applicationId` is `com.lanzmagick.kirinmal`. Older v1.0.0 packages and MoeList use `com.axiel7.moelist`, so this is a **separate installation**, not an overwrite or automatic migration. The internal Android/Kotlin namespace remains `com.axiel7.moelist` because it is code-only; Android resolves launcher activities with fully qualified class names. Shortcuts explicitly target the new application ID. The MAL OAuth redirect `moelist://moelist.page.link/` remains unchanged to avoid invalidating the existing MAL OAuth client registration. The user must sign into MAL again and may need to choose the right app if other apps register the same redirect scheme. Back up any local-only data before removing old installations.
