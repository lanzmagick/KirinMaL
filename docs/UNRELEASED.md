# KirinMaL — status build

APK signed terbaru ialah **v1.0.7 (414)**, diterbitkan pada **16 Oktober 2026, 10:29:23 MYT**.

## Patch terbaru — belum dibuild sebagai APK

- Tab **Genres** dibuang daripada Anime Menu dan Manga Menu, bersama bento kategori, hasil carian, cache, permintaan API genre, model khusus serta pengendali Back pada semua platform.
- Ujian dan dependency ujian yang hanya digunakan oleh browse genre turut dibuang. Tab ranking MAL menggunakan bilangan halaman asal tanpa halaman genre tambahan.
- Genre pada **Anime Info dan Manga Info** dikekalkan sepenuhnya: field MAL, model, nama terjemahan serta badge paparan tidak diubah. NSFW, Magic, login dan penyuntingan senarai tidak diubah oleh pembuangan genre.
- Pages dan README dikemas kini untuk menerangkan ranking serta genre pada Info, tanpa menjanjikan browse genre.
- Komponen menu ranking berjaya dicompile dan disemak secara offscreen pada kedua-dua tema untuk 18 keadaan ranking awal, termasuk fallback. Senarai ranking dan pembalut navigasi menggunakan data ujian; kod Info dan permintaan MAL disahkan kekal sama.
- **More → Settings → Magic** kini menyediakan tiga toggle tersimpan: **Enable Magic**, **Magic on Anime/Manga lists**, dan **Magic effects**. Default ketiga-tiganya ON.
- Magic utama OFF menyembunyikan dua slot Home serta Magic pada kedua-dua senarai. Dua pilihan lain disimpan dan menjadi tidak aktif sehingga Magic dihidupkan semula.
- Toggle senarai hanya mengawal butang Magic di Anime/Manga; Home kekal tersedia jika Magic utama ON. Toggle Random asal kekal berasingan.
- Efek OFF mengekalkan fungsi Magic dengan rupa statik: tiada partikel, border bergerak, kilauan kaca, halo, denyutan kotak misteri, transisi reveal atau delay kosmetik pemilihan.
- Pilihan tersimpan dibaca sebelum Magic atau animasinya dipaparkan. Bento membuang slot Magic yang dimatikan dan mengekalkan saiz kad lain, termasuk pada skrin tiga kolum.
- Popup **Magic Anime dan Magic Manga** mendapat partikel yang bergerak di border, jejak cahaya, pantulan kaca, halo poster dan percikan bintang ringkas ketika tajuk muncul.
- Anime menggunakan ungu–biru; Manga merah jambu–emas. Kotak misteri serta butang ikut palet yang sama dalam tema cerah/gelap.
- Popup dikongsi oleh Home serta kedua-dua senarai. Saiz maksimum, scrolling dan tindakan Open info, Surprise me again, Cancel serta dismiss dikekalkan.
- Efek dilukis tanpa recomposition atau layout pada setiap frame; animasi berterusan berhenti apabila entri navigasi tidak aktif. Tiada dependency atau permission baharu.
- Komponen sebenar berjaya dicompile dengan Compose. Semakan visual offscreen merangkumi kedua-dua tema dan jenis media, termasuk loading, empty, error, teks besar serta reveal, menggunakan data/poster ujian.
- Tiga ujian DataStore lulus dalam harness JVM, termasuk pilihan yang kekal selepas store dibuka semula, kebebasan daripada Random, serta pengekalan pilihan selepas Magic utama OFF/ON. Workflow release turut menjalankan ujian ini pada build seterusnya.
- Semakan visual ON/OFF meliputi Home, popup loading/hasil dan bahagian Settings pada kedua-dua tema; sepuluh perbandingan OFF kekal sama pada dua fasa animasi berbeza.
- Logik shuffle, sumber calon, tetapan Random asal, OAuth/login MAL dan version code dikekalkan. Pembuangan browse genre, patch popup serta tetapan Magic ini belum termasuk dalam APK v1.0.7.

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

## APK v1.0.6 yang sudah diterbitkan

Enam menu Home yang lebih padat sudah termasuk dalam APK: tinggi **60 dp**, padding **12 dp mendatar / 8 dp menegak**, ikon **20 dp** dan sudut **18 dp**. Saiz teks dan susunan kolum dikekalkan.

Butiran tersedia dalam [release notes v1.0.6](releases/v1.0.6.md).

- Ujian Magic dan pemilihan Recommended Manga lulus.
- Build Android signed dan build pengesahan Android lulus.
- Kelima-lima APK lulus semakan tandatangan, package ID, manifest dan permissions.
- Signing certificate sama seperti v1.0.5; APK boleh dipasang sebagai kemas kini.

**iOS dan Desktop kekal Coming soon.**
