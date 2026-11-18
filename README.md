# G-Swift Relay (Claude Mythos)

Sistem Informasi dan Pengiriman Email Cepat dengan **SMTP Relay**, **AI Email Designer**, **Manajemen Template**, **Konsol Log & API Key Rotation**, serta dukungan **PWA Installable** dan **Aplikasi Android Native (Kotlin + Jetpack Compose)**.

## Fitur Utama
- **Kirim Email SMTP**: Validasi & ekstraksi multi-penerima otomatis, dukungan HTML/Rich Text, pratinjau langsung, dan diagnosa error SMTP (termasuk Error 535 / App Password 16 karakter).
- **AI Email Designer (Server-side Key Rotation)**: Perancangan draf email responsif melalui backend `/api/chat-ai` dengan rotasi API Key otomatis multi-provider (Groq, Mistral, OpenRouter, Gemini).
- **Manajemen Template**: Penyimpanan template email lokal dengan fitur pratinjau dan kirim email uji coba.
- **Terminal & Monitor Rotasi API Key**: Log real-time dan kontrol cooldown key rotasi.
- **Konfigurasi SMTP 7-Layer**: Deteksi otomatis host/port dari domain via DNS MX/SRV serta verifikasi koneksi SMTP.

## Struktur Project
- `/` (`server.ts`, `keyRotator.ts`, `src/`): Aplikasi Web & Backend Express + React + Vite + PWA.
- `/android-native/`: Project Android Studio **100% Native** (Kotlin, Jetpack Compose, Material Design 3, MVVM, Repository Pattern, Coroutines/Flow, `EncryptedSharedPreferences`, tanpa `WebView`).
- `/.github/workflows/build-android.yml`: Workflow GitHub Actions untuk membangun Debug APK, Release APK, dan Play Store AAB secara otomatis.

## Menjalankan Aplikasi Web (Lokal)
1. Salin `.env.example` menjadi `.env` dan isi variabel lingkungan yang diperlukan.
2. Jalankan instalasi & server pengembangan:
   ```bash
   npm install
   npm run dev
   ```
3. Untuk build produksi:
   ```bash
   npm run build
   npm start
   ```

## Build Aplikasi Android Native
Lihat panduan lengkap di `android-native/README.md` atau jalankan workflow **Build Native Android APK & AAB** di tab **GitHub Actions**.
