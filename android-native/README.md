# G-Swift Relay — Native Android App (Kotlin + Jetpack Compose + Material 3)

Project Android Studio 100% Native tanpa `WebView`.

## Arsitektur
- **UI**: Jetpack Compose + Material Design 3 + Navigation Compose
- **Architecture**: MVVM (`GSwiftViewModel` + `StateFlow`) + Repository Pattern (`GSwiftRepository`)
- **Async & Reactive**: Kotlin Coroutines + Flow (`ConnectivityObserver` untuk status offline/online real-time)
- **Networking**: Retrofit 2 + OkHttp 4 + Gson terhubung langsung ke backend Express (`/api/*`)
- **Keamanan & Penyimpanan Lokal**: `EncryptedSharedPreferences` (AES-256) untuk kredensial SMTP + `SharedPreferences` untuk template & riwayat chat AI; tidak ada API Key AI yang disimpan di aplikasi Android (seluruh panggilan AI melalui `/api/chat-ai` di server).

## Build APK & AAB Otomatis via GitHub Actions
Workflow sudah tersedia di `.github/workflows/build-android.yml`:
1. Push repository ini ke GitHub (branch `main` atau `master`).
2. Buka tab **Actions** -> **Build Native Android APK & AAB** -> klik **Run workflow** (atau otomatis berjalan saat push ke `android-native/`).
3. Unduh hasil build dari bagian **Artifacts**:
   - `gswift-relay-debug-apk`: **APK Debug** (siap langsung diinstal di HP Android)
   - `gswift-relay-release-apk`: **APK Release**
   - `gswift-relay-release-aab`: **AAB Release** (untuk Google Play Store)

### (Opsional) Signing Release APK & Play Store AAB di GitHub Secrets
Tambahkan 4 secret di **Settings -> Secrets and variables -> Actions** agar APK Release & AAB otomatis ditandatangani:
- `ANDROID_KEYSTORE_BASE64`: hasil encode base64 file `.jks` (`base64 -w 0 release.jks`)
- `ANDROID_KEYSTORE_PASSWORD`: password keystore
- `ANDROID_KEY_ALIAS`: alias key
- `ANDROID_KEY_PASSWORD`: password key alias

## Build Lokal (Android Studio / Gradle)
```bash
cd android-native
gradle assembleDebug      # Debug APK
gradle assembleRelease    # Release APK
gradle bundleRelease      # Play Store AAB
```
