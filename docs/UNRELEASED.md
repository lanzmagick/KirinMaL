# KirinMaL — status build

APK signed terbaru ialah **v1.0.7 (414)**, diterbitkan pada **16 Oktober 2026, 10:29:23 MYT**.

## Patch yang sudah termasuk dalam APK v1.0.7

Release signed, ujian dan audit v1.0.7 sudah selesai. Lihat [release notes v1.0.7](releases/v1.0.7.md) dan [muat turun APK](https://github.com/lanzmagick/KirinMaL/releases/tag/v1.0.7).

- Dua butang Magic di Home mendapat border bercahaya dengan partikel berputar dan pantulan kaca menyerong yang bergerak perlahan: Anime ungu–biru, Manga merah jambu–emas.
- Saiz bento kekal **60 dp** dengan rupa yang menyesuaikan tema cerah/gelap. Animasi berterusan berhenti apabila entri navigasi tidak aktif; butang Magic/Random di senarai dan logik popup dikekalkan.
- Genre anime dan manga kembali menggunakan kategori asal MyAnimeList melalui Jikan v4, termasuk Genres, Themes, Demographics dan Explicit Genres.
- Hasil genre kini dicari menggunakan ID kategori MAL sebenar, bukan padanan genre/tag AniList. ID anime dan manga diasingkan.
- Nama kategori diambil daripada API; katalog lengkap yang berjaya dimuat disimpan dalam cache memori selama enam jam.
- Paging, retry bagi halaman gagal, penyingkiran ID berulang dan filter NSFW dikekalkan. Skor hasil menggunakan skala MAL **/10**.
- Susunan bento, navigasi Back, callback login dan penyuntingan senarai MAL dikekalkan.

## Pengesahan v1.0.7

- Ujian Magic, pemilihan Recommended Manga dan kontrak genre Jikan lulus dalam workflow penuh.
- Build Android signed dan build pengesahan Android lulus.
- Kelima-lima APK lulus semakan tandatangan v2, package ID, manifest dan permissions sebelum diterbitkan.
- Signing certificate sama seperti v1.0.6; APK boleh dipasang sebagai kemas kini.
- Sumber APK: commit `142872f4b3f4005dc4b9b4e674594527e04adb83`.
- [Workflow signed](https://github.com/lanzmagick/KirinMaL/actions/runs/38840148281) · [Workflow pengesahan](https://github.com/lanzmagick/KirinMaL/actions/runs/38840148291).

Tiada patch sumber baharu yang menunggu build APK.

## APK v1.0.6 yang sudah diterbitkan

Enam menu Home yang lebih padat sudah termasuk dalam APK: tinggi **60 dp**, padding **12 dp mendatar / 8 dp menegak**, ikon **20 dp** dan sudut **18 dp**. Saiz teks dan susunan kolum dikekalkan.

Butiran tersedia dalam [release notes v1.0.6](releases/v1.0.6.md).

- Ujian Magic dan pemilihan Recommended Manga lulus.
- Build Android signed dan build pengesahan Android lulus.
- Kelima-lima APK lulus semakan tandatangan, package ID, manifest dan permissions.
- Signing certificate sama seperti v1.0.5; APK boleh dipasang sebagai kemas kini.

**iOS dan Desktop kekal Coming soon.**
