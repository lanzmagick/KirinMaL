# KirinMaL — status build

APK signed terbaru ialah **v1.0.11 (418)**, diterbitkan pada **17 Oktober 2026, 02:24:42 MYT**.

Release signed, ujian dan audit v1.0.11 sudah selesai. Lihat [release notes v1.0.11](releases/v1.0.11.md) dan [muat turun APK](https://github.com/lanzmagick/KirinMaL/releases/tag/v1.0.11).

## Patch yang sudah termasuk dalam APK v1.0.11

- Kawalan dropdown di kanan search bar Anime/Manga menggunakan ikon tiga garis sahaja, mengikut susunan ikon pada [list AniHyou](https://github.com/axiel7/AniHyou-android/blob/master/feature/usermedialist/src/main/java/com/axiel7/anihyou/feature/usermedialist/search/TopSearchBar.kt).
- Ikon 24 dp berada dalam kawasan sentuh 48 dp, tanpa label pilihan atau bingkai chip pada bar atas. Pilihan seperti Last updated dan tanda pilihan semasa tersedia apabila menu dibuka; pembaca skrin masih menerima nama pilihan.
- Padanan tab yang dibetulkan dalam v1.0.10, simpanan sort, penapis A–Z, Random dan Magic dikekalkan.

## Pengesahan v1.0.11

- Ujian regresi dropdown/tab, Magic shuffle, simpanan tetapan Magic dan Recommended Manga serta kedua-dua build Android lulus.
- Semua lima APK lulus audit tandatangan v2, manifest, permission dan identiti app. Hash SHA-256 aset GitHub Release sepadan dengan log audit.
- Sijil signing sama seperti v1.0.10; APK boleh dipasang sebagai kemas kini pada versi itu.
- Sumber APK: commit `ca0baeecc60ec3aff1d0c61fc3a987b6949ab695`.
- [Workflow signed](https://github.com/lanzmagick/KirinMaL/actions/runs/38946418045) · [Workflow pengesahan](https://github.com/lanzmagick/KirinMaL/actions/runs/38946418092).
- Belum diuji pada peranti fizikal.

## Patch yang sudah termasuk dalam APK v1.0.10

- Dropdown asal list Anime/Manga dipulihkan di kanan search bar dengan label pilihan semasa seperti **Last updated**.
- Padanan tab kini mengenal pasti instans tab Anime/Manga dengan betul. Ini membetulkan dropdown yang hilang dalam v1.0.9 selepas kawalan lama dipindahkan.
- Bentuk butang berlabel dipulihkan. Menu Title, Score, Last updated dan Start date serta simpanan pilihan berasingan dikekalkan.
- Penapis huruf Sort: All/A–Z, Random asal dan Magic kekal di toolbar senarai. OAuth/login MAL, permission dan dependency runtime dikekalkan.

## Pengesahan v1.0.10

- Empat ujian regresi meliputi tab Anime/Manga sebenar, instans tab yang dipulihkan, keadaan belum login dan halaman lain; semuanya lulus dalam workflow release.
- Ujian Magic shuffle, tetapan Magic dan Recommended Manga serta kedua-dua build Android lulus.
- Kelima-lima APK lulus audit tandatangan v2, manifest, permission dan identiti app. Hash SHA-256 aset GitHub Release sepadan dengan log audit.
- Sijil signing sama seperti v1.0.9; APK boleh dipasang sebagai kemas kini pada versi itu.
- Sumber APK: commit `060836e365a77db7a27243fced3c96ee9e6b053a`.
- [Workflow signed](https://github.com/lanzmagick/KirinMaL/actions/runs/38939983232) · [Workflow pengesahan](https://github.com/lanzmagick/KirinMaL/actions/runs/38939983483).
- Belum diuji pada peranti fizikal. Ujian regresi mengesahkan logik tab sebenar; semakan build tidak menggantikan ujian paparan pada telefon.

## Patch yang sudah termasuk dalam APK v1.0.9

**Isu yang disahkan:** dropdown tidak muncul kerana padanan tab tersilap. Gunakan **v1.0.10** untuk pembetulan.

- Menu sort Anime dan Manga pada tampilan telefon dipindahkan ke ikon di kanan search bar. Pilihan Last updated, Title, Score dan Start date kekal tersedia dengan tanda pilihan semasa.
- Search bar menyesuaikan lebar; avatar profil dan kawasan sentuh sort 56 dp dikekalkan.
- Anime dan Manga menggunakan tetapan sort tersimpan yang berasingan. List aktif dan tab status yang disimpan memuat semula pilihan tersebut tanpa menukar status atau penapis huruf.
- Sort pada toolbar lama disembunyikan apabila kawalan search bar digunakan. Senarai pada tampilan lebar serta halaman tanpa search bar mengekalkan kawalan sort sedia ada.
- Helper source **KIRINMAL-PASSWORD.cmd** menjana signing key dengan alias dan butiran sijil yang sudah diisi; pengguna hanya memasukkan password.

## Pengesahan v1.0.9

- Ujian shuffle Magic, pilihan Magic dalam DataStore dan pemilihan Recommended Manga lulus.
- Build signed dan build pengesahan Android lulus. Ini mengesahkan compile yang sebelumnya tersekat oleh sambungan dependency tempatan.
- Semua lima APK lulus audit tandatangan v2, package ID, manifest dan permissions. Hash SHA-256 aset GitHub Release sepadan dengan log audit.
- Sijil signing build ini **sama seperti v1.0.8**, berdasarkan audit APK sebenar. Key baharu yang dijana secara tempatan belum digunakan dalam APK ini.
- Sumber APK: commit `8848582f0cead17545069d7706a91eab7f73f1b5`.
- [Workflow signed](https://github.com/lanzmagick/KirinMaL/actions/runs/38931959172) · [Workflow pengesahan](https://github.com/lanzmagick/KirinMaL/actions/runs/38931959150).
- Patch sort belum diuji pada peranti fizikal; render paparan tempatan tidak diselesaikan kerana sambungan dependency terputus. Audit build tidak menentukan klasifikasi Google Play Protect.

## Patch yang sudah termasuk dalam APK v1.0.8

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
- Tiga ujian DataStore lulus dalam harness JVM, termasuk pilihan yang kekal selepas store dibuka semula, kebebasan daripada Random, serta pengekalan pilihan selepas Magic utama OFF/ON. Suite ini juga lulus dalam workflow release v1.0.8.
- Semakan visual ON/OFF meliputi Home, popup loading/hasil dan bahagian Settings pada kedua-dua tema; sepuluh perbandingan OFF kekal sama pada dua fasa animasi berbeza.
- Logik shuffle, sumber calon, tetapan Random asal dan OAuth/login MAL dikekalkan. Pembuangan browse genre, patch popup serta tetapan Magic ini sudah termasuk dalam APK v1.0.8 (415).

## Pengesahan v1.0.8

- Ujian shuffle Magic, pilihan Magic dalam DataStore dan pemilihan Recommended Manga lulus dalam workflow penuh.
- Build Android signed dan build pengesahan Android lulus.
- Kelima-lima APK lulus semakan tandatangan v2, package ID, manifest dan permissions sebelum diterbitkan. Semua hash SHA-256 aset release sepadan dengan log audit.
- Signing certificate sama seperti v1.0.7; APK boleh dipasang sebagai kemas kini.
- Sumber APK: commit `cf94d673bf967a0fa25d99bc78345dd7e0080977`.
- [Workflow signed](https://github.com/lanzmagick/KirinMaL/actions/runs/38855000245) · [Workflow pengesahan](https://github.com/lanzmagick/KirinMaL/actions/runs/38855000246).
- Pengesahan UI terdahulu menggunakan render offscreen dengan data ujian. Release ini belum diuji pada peranti fizikal.

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
