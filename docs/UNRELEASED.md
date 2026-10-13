# KirinMaL — patch belum dibina

APK signed terbaru kekal **v1.0.4 (411)**. Perubahan berikut sudah disediakan pada source dan menunggu arahan build.

## Label filter list

- Anime dan Manga menggunakan **Sort : All**, **Sort : A**, **Sort : 1** atau **Sort : #**, mengikut pilihan permulaan tajuk.

## Magic dalam list

- Magic sentiasa muncul pada toolbar Anime dan Manga walaupun toggle **Random button on anime/manga list** dimatikan.
- Toggle tersebut hanya mengawal butang Random asal. Pilihan yang disimpan dan logik Random dikekalkan; pengguna boleh mematikannya sendiri untuk menggunakan Magic sahaja.

## Home

- Buang butang Random Anime dan Random Manga daripada Home; kekalkan Magic Anime dan Magic Manga dengan popup sedia ada.
- Gabungkan kedua-dua Magic dengan empat shortcut Home dalam bento bersaiz seragam: dua kolum di telefon dan tiga kolum di skrin besar, tinggi kad 84 dp dan jarak 8 dp.
- Buang slot **Popular Manga** dan **Top Rated Manga** daripada Home; kedua-dua ranking masih tersedia dalam Manga Menu.
- Susunan slot peribadi selepas shortcut Home: **Recent Watching Anime → Reading Manga → Recommended Manga**. Muncul selepas login.
- Recent Watching Anime mengambil sehingga 25 tajuk status Watching, disusun mengikut kemas kini MAL terbaru, dengan progress episod dan badge Watching. Tajuk slot membuka list Watching.
- Recommended Manga menggabungkan cadangan MAL daripada sehingga tiga manga Reading yang baru dikemas kini; gunakan Completed jika Reading kosong. Gabungkan jumlah undian cadangan, buang ID berganda dan manga yang sudah berada dalam mana-mana status list pengguna.
- Ikut bahasa tajuk, Hide Scores dan NSFW; ketika NSFW disembunyikan, calon tanpa metadata keselamatan turut disembunyikan.
- Home memaparkan sehingga 25 cadangan; anak panah membuka grid penuh sehingga 100 cadangan daripada sumber yang sama.
- Slot memuat secara berasingan, mempunyai mesej kosong/Retry, refresh selepas Quick Edit dan mengosongkan data peribadi ketika logout.

## Rupa login MAL

- Tambah tajuk MyAnimeList, ikon buku, butang biru MAL yang lebih lebar, label **Continue with MyAnimeList**, monogram MAL dan ikon anak panah.
- Callback `event?.openLogin(browserHandler)`, `continueLogin()`, LoginViewModel, OAuthService, redirect, Client ID dan token handling dikekalkan.

## About

- Tambah toggle **Settings → Display → Show About menu**, default OFF. Hanya sembunyikan pintasan About dalam More; halaman About, pautan dan Credits asal masih tersedia apabila toggle dihidupkan.

## Semakan

- Floating status Anime/Manga diperiksa sahaja: border 1.5 dp pada status terpilih menggunakan warna teks/ikon, hitam pada Watching/Reading, On Hold dan Plan. Kod floating tidak diubah.
- Semakan statik sintaks Kotlin, resource XML, sambungan route dan kod login.
- Endpoint cadangan MAL berjaya memberi respons dengan metadata skor dan NSFW.
- Unit test pemilihan cadangan ditambah untuk ID berganda, ranking undian, pengecualian list, NSFW, limit dan jumlah undian besar. Workflow build akan menjalankannya bersama ujian Magic.
- **Build Android, test Kotlin dan release APK untuk patch ini belum dijalankan.**
