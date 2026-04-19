# AIBOBSCSTEWA Android App

A native Android companion app for the **All India Bank of Baroda SC & ST Employees' Welfare Association** website (https://aibobscstewa.com).

It wraps the official website in a polished native shell with a side navigation drawer mapped to every key section of the association, splash screen, pull-to-refresh, offline detection, PDF/circular downloads, and proper back-button handling.

---

## ⚡ How to get an APK

You have three realistic options. Pick the one that fits you.

### Option A — GitHub Actions (zero local setup, free, cloud-built APK)

Fastest way to get an installable APK without installing Android Studio.

1. Create a free account at https://github.com if you don't have one.
2. Create a new **empty** repository (private is fine).
3. Upload this entire project folder to that repo (GitHub's web UI supports drag-and-drop of a zip, or use `git push`).
4. Go to the **Actions** tab on your repo — the `Build APK` workflow will run automatically (~5–8 minutes the first time).
5. When it finishes (green check), click into the run, scroll down to **Artifacts**, and download **`AIBOBSCSTEWA-debug-apk`**.
6. Transfer the `app-debug.apk` to your Android phone and install (allow "install from unknown sources" when prompted).

The workflow file is already included at `.github/workflows/build-apk.yml` — no setup needed.

### Option B — Android Studio (local, best for ongoing development)

1. Install **Android Studio Giraffe (2023.1.1)** or newer from https://developer.android.com/studio.
2. `File → Open…` → select the `AIBOBSCSTEWA-App` folder.
3. Wait for Gradle sync (first time: 5–10 minutes while dependencies download).
4. `Build → Build Bundle(s) / APK(s) → Build APK(s)`.
5. When the notification shows "APK(s) generated successfully", click **locate** → `app/build/outputs/apk/debug/app-debug.apk`.

### Option C — Command line (if you already have Android SDK + Java 17)

```bash
cd AIBOBSCSTEWA-App
gradle wrapper --gradle-version 8.2   # first time only
chmod +x gradlew
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Requires `ANDROID_HOME` pointing to an Android SDK install with `build-tools;34.0.0` and `platforms;android-34` installed.

---

## Installing the APK on your phone

1. Copy the APK to your phone (USB, email, Google Drive, etc.)
2. On the phone, tap the APK in your Files app
3. Approve "install from this source" when prompted
4. Tap **Install**

The debug APK is signed with a throwaway debug key — fine for personal/testing use, **not** for Play Store. For Play Store you'll need a real signing key; see "Publishing to Google Play" below.

---

## Features

- **Branded splash screen** in AIBOB orange and navy blue with the three-figures unity logo (1.6s)
- **Navigation drawer** with every key section of the association:
  - Home
  - About Us / Our Objectives / Reservation Policy
  - Central Committee / Office Bearers
  - Circulars / Notices / News / Events / Photo Gallery / Downloads
  - Contact Us / Share App / About App
- **Swipe-to-refresh** on every page
- **Top progress bar** while pages load
- **Offline screen** with retry button
- **File downloads** — circulars, notices and PDFs download via Android's `DownloadManager` with a notification
- **Smart link handling** — aibobscstewa.com stays in-app; external links open in the browser; `tel:`, `mailto:` open in native apps
- **Proper back navigation** — back button navigates WebView history, then exits
- **Share** — share the app from the drawer
- **Material 3** theme with dynamic colors on Android 12+
- **Dark mode** supported
- **Adaptive launcher icon** (Android 8.0+) with monochrome themed icon for Android 13+
- **minSdk 21** (Android 5.0 Lollipop) → works on ~99% of active devices

## Project layout

```
AIBOBSCSTEWA-App/
├── .github/workflows/build-apk.yml   ← cloud build config
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/in/aibobscstewa/app/
│       │   ├── AIBOBApplication.kt
│       │   ├── SplashActivity.kt
│       │   ├── MainActivity.kt
│       │   └── AboutActivity.kt
│       └── res/
│           ├── drawable/       (icons + three-figures logo)
│           ├── layout/         (splash, main, about, nav header)
│           ├── menu/           (drawer menu)
│           ├── mipmap-*/       (launcher icons, all densities)
│           ├── values/         (colors, strings, themes)
│           ├── values-night/   (dark theme)
│           └── xml/            (network security config)
├── build.gradle
├── settings.gradle
├── gradle.properties
└── gradle/wrapper/gradle-wrapper.properties
```

## Customising

| What | Where |
|------|-------|
| App name | `res/values/strings.xml` → `app_name` |
| Theme colors | `res/values/colors.xml` (`aibob_orange`, `aibob_blue`) |
| Home URL | `MainActivity.kt` → `homeUrl` |
| Drawer menu items | `res/menu/drawer_menu.xml` + `onNavigationItemSelected` in `MainActivity.kt` |
| Splash duration | `SplashActivity.kt` → `splashDurationMs` |
| App icon | replace `res/mipmap-*/ic_launcher.png` and `res/drawable/ic_launcher_foreground.xml` |
| Package / application ID | `app/build.gradle` → `namespace` and `applicationId` (currently `in.aibobscstewa.app`) |

## Menu → URL mapping

The drawer links are pre-configured to the expected section URLs. If the live site uses different slugs, edit the `when (item.itemId) { ... }` block in `MainActivity.kt`:

| Menu item | URL |
|---|---|
| Home | `https://aibobscstewa.com/` |
| About Us | `https://aibobscstewa.com/about/` |
| Our Objectives | `https://aibobscstewa.com/objectives/` |
| Reservation Policy | `https://aibobscstewa.com/reservation-policy/` |
| Central Committee | `https://aibobscstewa.com/committee/` |
| Office Bearers | `https://aibobscstewa.com/office-bearers/` |
| Circulars | `https://aibobscstewa.com/circulars/` |
| Notices | `https://aibobscstewa.com/notices/` |
| News | `https://aibobscstewa.com/news/` |
| Events | `https://aibobscstewa.com/events/` |
| Photo Gallery | `https://aibobscstewa.com/gallery/` |
| Downloads | `https://aibobscstewa.com/downloads/` |
| Contact Us | `https://aibobscstewa.com/contact/` |

## Publishing to Google Play

1. **Get explicit permission from AIBOBSCSTEWA office bearers** to publish an app that wraps the association's site — Google Play policies require ownership of the content or explicit authorisation.
2. Generate a signing key:
   ```bash
   keytool -genkey -v -keystore aibobscstewa-release.keystore -alias aibobscstewa -keyalg RSA -keysize 2048 -validity 10000
   ```
3. Add a signing config to `app/build.gradle` (inside `android { ... }`):
   ```groovy
   signingConfigs {
       release {
           storeFile file("../aibobscstewa-release.keystore")
           storePassword "YOUR_PASSWORD"
           keyAlias "aibobscstewa"
           keyPassword "YOUR_PASSWORD"
       }
   }
   buildTypes { release { signingConfig signingConfigs.release } }
   ```
4. Build an AAB: `./gradlew bundleRelease` → upload `app/build/outputs/bundle/release/app-release.aab` to Play Console.

## Notes & disclaimers

- This is an **unofficial companion app**. All content is served live from https://aibobscstewa.com — the app does not store, cache permanently, or republish any content.
- Network security config permits cleartext traffic to `aibobscstewa.com` for compatibility with any mixed-content resources the site may serve. Tighten to HTTPS-only once confirmed.
- The launcher icon is a simple three-figures unity monogram in AIBOB orange + navy. Replace with the official association logo before release.
- If the association later publishes a native mobile API or JSON feed, the app can migrate to a fully-native UI (RecyclerView lists for circulars/notices) with minimal changes — the activity and navigation structure is already in place.
