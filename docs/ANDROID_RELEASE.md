# KirinMaL — Android Signed Releases

The first public signed release was v1.0.0. **v1.0.1** introduces a distinct Android application ID; **v1.0.2** audits the signed APK before publication. Builds are distributed through **GitHub Releases**, never as debug APKs or Actions artifacts.

## Four GitHub Actions secrets

The owner must add these values under **Settings → Secrets and variables → Actions → New repository secret**:

1. `KEYSTORE_FILE` — full base64 of the private `.jks` signing keystore, on a single line.
2. `KEYSTORE_PASSWORD` — password to unlock the keystore.
3. `KEY_ALIAS` — alias of the signing key inside the keystore.
4. `KEY_PASSWORD` — password to unlock the signing key.

These values come from the owner's privately provided **KirinMaL-4-Kunci-Release.zip** file and **must never be committed to the repository** or published as an artifact.

## Windows: CMD dengan password sendiri

Download **[JANA-KEY-KIRINMAL.cmd](../tools/signing/JANA-KEY-KIRINMAL.cmd)** dan double-click, atau jalankan dari terminal:

```powershell
.\JANA-KEY-KIRINMAL.cmd
```

Java JDK perlu tersedia melalui `JAVA_HOME` atau `PATH`. Pilih alias, kemudian isi password keystore, butiran pemilik dan password key apabila `keytool` bertanya. Password ditaip terus ke `keytool`, tanpa dipaparkan atau disimpan oleh skrip. Tekan Enter pada soalan password key jika mahu menggunakan password keystore yang sama.

Skrip menghasilkan folder **KirinMaL-key-sendiri** di sebelah CMD:

- **KirinMaL-release.jks** — private keystore.
- **KEYSTORE_FILE.txt** — base64 pada satu baris untuk secret `KEYSTORE_FILE`.
- **KEY_ALIAS.txt** — alias yang dipilih.
- **CARA-ISI-SECRETS.txt** — panduan empat secret, menggunakan password yang dipilih sendiri.

Masukkan empat nilai itu sendiri ke **Settings → Secrets and variables → Actions**. Skrip ini tidak memasang GitHub CLI, meminta login GitHub, mengupload secret atau memulakan build. Fail JKS yang sudah wujud tidak ditimpa.

**CMD ini menjana key baharu.** Untuk terus mengemas kini pemasangan KirinMaL sedia ada, gunakan keystore release asal; key baharu mempunyai tandatangan berbeza. Pakej asal **KirinMaL-4-Kunci-Release.zip** masih menyimpan keystore asal. Menjalankan CMD ini tidak menukar key atau secret release yang sedang digunakan.

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
