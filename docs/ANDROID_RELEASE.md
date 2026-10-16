# KirinMaL — Android Signed Releases

The first public signed release was v1.0.0. **v1.0.1** introduces a distinct Android application ID; **v1.0.2** audits the signed APK before publication. Builds are distributed through **GitHub Releases**, never as debug APKs or Actions artifacts.

## Four GitHub Actions secrets

The owner must add these values under **Settings → Secrets and variables → Actions → New repository secret**:

1. `KEYSTORE_FILE` — full base64 of the private `.jks` signing keystore, on a single line.
2. `KEYSTORE_PASSWORD` — password to unlock the keystore.
3. `KEY_ALIAS` — alias of the signing key inside the keystore.
4. `KEY_PASSWORD` — password to unlock the signing key.

These values come from the owner's privately provided **KirinMaL-4-Kunci-Release.zip** file and **must never be committed to the repository** or published as an artifact.

## Windows: paste one command

The private bundle includes **SETUP-KIRINMAL.bat** and **setup-signing.ps1** beside the existing keystore and secrets file. Extract the ZIP, open Windows Terminal in the extracted folder, then paste:

```powershell
.\SETUP-KIRINMAL.bat
```

The helper checks the original keystore's SHA-256 and verifies that `KEYSTORE_FILE` decodes to the same file before doing anything online. It uses GitHub CLI to set the four existing secret names on **lanzmagick/KirinMaL**. If needed, it installs GitHub CLI through WinGet and starts GitHub's browser login. Sign in with the account that can manage this repository's Actions secrets. Without WinGet, install [GitHub CLI](https://cli.github.com/) first and run the same command again.

Secret values are read from the private file and passed through stdin, without appearing in terminal output or process arguments. The helper does not generate a new key, change `MAL_CLIENT_ID`, or start a build. If an upload fails partway through, rerun it to finish setting the same four values.

To check the local bundle without installing software, logging in or uploading anything:

```powershell
.\SETUP-KIRINMAL.bat -CheckOnly
```

The public helper also lives under `tools/signing/`. From a repository checkout, point it at the extracted private bundle:

```powershell
.\tools\signing\SETUP-KIRINMAL.bat -BundlePath "C:\path\to\KirinMaL-4-Kunci-Release"
```

Keep the two helper files together. The helper runs with Windows PowerShell 5.1 or later; Java is not required. The signing key is the same one used by the existing KirinMaL releases, so APK update compatibility is preserved.

## MyAnimeList Client ID

Signed builds also require the owner's **`MAL_CLIENT_ID`** repository secret. Register the MAL API application with the exact redirect `kirinmal://oauth/mal`, then copy its Client ID into this secret. See [MAL Client ID instructions](MAL_CLIENT_ID.md).

## Publish stable APKs

1. Back up the private JKS file and passwords. If the keystore is lost, future Android updates cannot use the same signature.
2. Set all four signing secrets and `MAL_CLIENT_ID`.
3. Go to **Actions → KirinMaL Signed Release → Run workflow → main**.
4. Wait for **Build signed production APKs**, **Verify every APK has an Android release signature**, **Audit signed APK manifest, permissions and identity**, and **Publish audited KirinMaL Android release** to succeed.
5. Open **Releases → the version from `version.properties`** and download the matching device APK. The draft only becomes public **after** signed APKs have been uploaded and verified.
6. The old `development` release containing debug APKs is removed during first-release preparation or successful signed publication.

The `Verify KirinMaL Android Release` workflow compiles an **unsigned test release** on pushes to check Kotlin/Android compilation; it **never publishes APKs**.

**Android identity:** From v1.0.1 onwards the release `applicationId` is `com.lanzmagick.kirinmal`. Older v1.0.0 packages and MoeList use `com.axiel7.moelist`, so this is a **separate installation**, not an overwrite or automatic migration. The internal Android/Kotlin namespace remains `com.axiel7.moelist` because it is code-only; Android resolves launcher activities with fully qualified class names. Shortcuts explicitly target the new application ID. KirinMaL has its own MAL OAuth redirect `kirinmal://oauth/mal` and `MAL_CLIENT_ID` secret; KirinNime's distinct registration uses `kirinime://oauth/mal`. Changing clients requires logging in again. Back up any local-only data before removing old installations.
