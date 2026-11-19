<div align="center">
  <img src="app_icon.png" alt="Logo CookCompile" width="112" />
  <h1>CookCompile — Buku Resep Digital Nusantara</h1>
  <p>
    <em>"Sering bingung mau masak apa setiap buka kulkas? CookCompile merangkum lebih dari 1.050 resep masakan rumahan, lauk pauk Nusantara, sambal ulek, hingga jajanan pasar dan minuman segar dalam satu aplikasi Android yang ringan, rapi, dan 100% ditulis dengan Kotlin."</em>
  </p>
  <p>
    <img src="https://img.shields.io/badge/100%25%20Pure-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="100% Pure Kotlin" />
    <img src="https://img.shields.io/badge/Design-Material%20Design%203-2E6F40?style=flat-square&logo=materialdesign&logoColor=white" alt="Material Design 3" />
    <img src="https://img.shields.io/badge/Katalog-1.050%2B%20Resep%20Nusantara-F59E0B?style=flat-square" alt="1.050+ Resep Nusantara" />
    <img src="https://img.shields.io/badge/Data-Offline%20%2B%20Cloud%20Sync-059669?style=flat-square" alt="Offline + Cloud Sync" />
    <img src="https://img.shields.io/badge/Platform-Android%207.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android 7.0+" />
  </p>
</div>

---

## 🥘 Hari Ini Masak Apa?

Buat kamu yang lagi cari ide masak makan siang, bekal praktis, camilan sore, atau sekadar belajar resep baru di dapur—langsung saja pasang **CookCompile** di HP Android kamu!

| Unduh Cepat | Keterangan |
| :--- | :--- |
| 📦 **[`CookCompile.apk`](CookCompile.apk)** | File installer Android siap pasang (`minSdk 24` / Android 7.0 ke atas) |
| 🔓 **Mode Tanpa Akun** | Tekan **Lihat Resep Tanpa Login** di halaman masuk untuk langsung jelajahi 1.050+ resep tanpa registrasi |

> **Tips:** Sudah punya bahan seadanya di rumah? Cukup ketik nama bahannya di kolom pencarian (misalnya `tempe`, `telur`, atau `ayam`), dan CookCompile akan langsung menampilkan menu yang siap kamu olah.

---

## 🍛 Jelajahi 9 Kategori Dapur Nusantara

Seluruh koleksi resep dikelompokkan rapi ke dalam kategori **Masakan Utama** dan **Cemilan & Minuman** dengan total **1.050 resep** siap coba:

| Kategori | Jenis | Jumlah Menu | Contoh Hidangan Populer |
| :--- | :---: | :---: | :--- |
| **Ayam** | Masakan | 107 | Ayam Goreng Lengkuas, Ayam Rica-Rica, Pepes Ayam Jamur |
| **Daging** | Masakan | 107 | Rendang Daging Padang, Rawon Sapi, Tongseng Kuah Gurih |
| **Ikan & Seafood** | Masakan | 107 | Ikan Bakar Jimbaran, Udang Saus Tiram, Cumi Saus Padang |
| **Tahu & Tempe** | Masakan | 107 | Bacem Tahu Tempe, Orek Tempe Manis, Mendoan Renyah |
| **Nasi & Mie** | Masakan | 107 | Nasi Goreng Kampung, Nasi Liwet Sunda, Mie Goreng Jawa |
| **Sayur & Sup** | Masakan | 105 | Sayur Asem Jakarta, Sayur Lodeh, Sop Oyong Jamur Enoki |
| **Camilan** | Cemilan | 205 | Klepon Pandan, Pisang Goreng Krispi, Risoles Mayo, Siomay |
| **Minuman Segar** | Cemilan | 106 | Es Cendol Dawet, Es Kuwut Melon, Es Pisang Ijo Makassar |
| **Aneka Sambal** | Masakan | 99 | Sambal Matah Bali, Sambal Ijo Padang, Sambal Bajak |

---

## ✨ Kenapa Enak Dipakai di Dapur?

1. **Cari Berdasarkan Bahan di Kulkas**
   Punya stok tahu, jamur enoki, atau daging sapi? Ketik kata kuncinya di bilah pencarian beranda—sistem memfilter berdasarkan judul resep, kategori, penulis, sekaligus daftar bahan.
2. **Takaran & Langkah Anti-Bingung**
   Setiap halaman detail resep menyajikan daftar bahan lengkap, langkah pembuatan bernomor yang jelas, estimasi waktu masak, porsi saji, kalori, tingkat kesulitan, fitur bagikan ke WhatsApp/sosial media, serta tautan langsung ke referensi resep di Cookpad.
3. **Ringan & Anti-Lag dengan Pemuatan 15 Menu Bertahap**
   Katalog populer horizontal dan daftar resep vertikal dimuat per **15 resep** menggunakan pagination otomatis saat *scroll* ke bawah agar aplikasi tetap halus di berbagai spesifikasi HP. Tarik layar ke bawah (*Pull-to-Refresh*) kapan saja untuk menyegarkan dan mengecek pembaruan katalog.
4. **Rak Resep Favorit Pribadi**
   Ketuk ikon *bookmark* di kartu menu atau halaman detail untuk menyimpan resep andalan ke tab **Tersimpan**. Semua simpanan tersimpan rapi secara lokal dan bisa dikelola kapan saja.

---

## 🧩 Arsitektur 100% Native Kotlin

CookCompile dibangun sepenuhnya dengan **Kotlin** dan **Android XML + ViewBinding** tanpa komponen JavaScript atau WebView:

| Komponen | Lokasi | Peran |
| :--- | :--- | :--- |
| **`SplashActivity.kt`** | `app/src/main/java/com/example/cookcompileapp/` | Halaman pembuka dengan ilustrasi kuliner dan tombol CTA mulai memasak |
| **`LoginActivity.kt`** | `app/src/main/java/com/example/cookcompileapp/` | Masuk akun dengan validasi email atau akses cepat mode tamu |
| **`MainActivity.kt`** | `app/src/main/java/com/example/cookcompileapp/` | Pengelola navigasi 3 tab utama (*Beranda*, *Tersimpan*, *Profil*) |
| **`HomeFragment.kt`** | `app/src/main/java/com/example/cookcompileapp/` | Pencarian *real-time*, filter chip kategori, carousel populer, dan *endless scroll* |
| **`SavedFragment.kt`** | `app/src/main/java/com/example/cookcompileapp/` | Daftar koleksi resep favorit, hapus massal, serta pemulihan favorit awal |
| **`ProfileFragment.kt`** | `app/src/main/java/com/example/cookcompileapp/` | Statistik resep, sinkronisasi katalog cloud (OTA), informasi aplikasi, & keluar akun |
| **`DetailRecipeActivity.kt`** | `app/src/main/java/com/example/cookcompileapp/` | Tampilan detail resep lengkap dengan *adapter* bahan & langkah memasak |
| **`RecipeRepository.kt`** | `.../cookcompileapp/repository/` | Lapisan data utama: baca `assets/recipes.json`, cache lokal, favorit, & OTA sync |
| **`RecipeCatalogGenerator.kt`** | `.../cookcompileapp/repository/` | Engine generator katalog 1.050+ resep berbahasa Kotlin murni |

---

## 🛠️ Cara Menjalankan Proyek di Android Studio

### Prasyarat
- **Android Studio** terbaru (mendukung Android Gradle Plugin `9.3.3`)
- **JDK 21** atau lebih baru
- **Android SDK** (`compileSdk 37`, `targetSdk 36`, `minSdk 24`)

### Langkah Build
1. Clone repositori ini ke komputer kamu:
   ```bash
   git clone https://github.com/wigataLK/CookCompile.git
   cd CookCompile
   ```
2. Buka folder `CookCompile` di **Android Studio** lalu tekan **Run**, atau build APK langsung melalui terminal:
   - **macOS / Linux:**
     ```bash
     ./gradlew :app:assembleDebug
     ```
   - **Windows PowerShell:**
     ```powershell
     .\gradlew.bat :app:assembleDebug
     ```
3. File APK hasil kompilasi akan tersedia di `app/build/outputs/apk/debug/app-debug.apk`.
