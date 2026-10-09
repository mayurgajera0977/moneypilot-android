MONEYPILOT ANDROID STUDIO PROJECT (SOURCE)

IMPORTANT: This is a native Android WebView wrapper and home-screen AppWidget source project. It is not a precompiled APK. The available build environment had no Android SDK/Gradle toolchain and no network access, so an APK could not be compiled here.

BUILD ON YOUR PC
1. Install Android Studio and the Android SDK Platform 35 (or adjust compileSdk to an installed SDK).
2. Extract this folder.
3. In Android Studio choose Open and select this folder.
4. Allow Gradle sync and let Android Studio download the Android Gradle Plugin/dependencies.
5. Select Build > Build Bundle(s) / APK(s) > Build APK(s).
6. The debug APK is normally created under app/build/outputs/apk/debug/app-debug.apk.
7. Transfer the APK to your Android phone and install it. Android may ask you to allow installs from that source.
8. Long-press your Android home screen, add the MoneyPilot widget, then use Expense or Income.

WHAT THIS BUILD DOES
- Bundles the current MoneyPilot HTML app for offline use inside a WebView.
- Adds a native Android home-screen widget with Expense and Income buttons.
- Widget buttons open the app's quick-entry form and preselect transaction type.
- Local browser storage persists app data on that device. CSV/JSON downloads are routed to Downloads/MoneyPilot. File selection is enabled for restore/import.

LIMITATIONS
- This wrapper uses the existing local HTML data store; it does not implement Supabase cloud sync or multi-device sync.
- Android APK and PC browser data are separate and do not sync automatically.
- This project has not been compiled or tested on a physical Android device in this environment.
- For commercial distribution, add a proper app icon, signing key, release build, privacy policy and device QA. Never share your signing key.

ACCOUNT PROFILE UPDATE
The bundled MoneyPilot web app now includes local profile creation and login by Indian mobile number + password. Profiles have separate local browser/WebView storage. SMS OTP and cloud sync are not configured; a phone number is not verified. Keep backups. The Android shell does not automatically sync data with the PC browser.


LIGHT UI & WIDGET PERSONALISATION
- Updated bundled UI to a light pastel design.
- Settings includes Aurora, Fresh Mint, Peach Glow, Minimal and Glass Glow widget styles.
- In the Android wrapper, selecting a style calls the native bridge and refreshes installed home-screen widgets.
- The native widget style is stored in Android SharedPreferences.

FAST BUILD OPTION
See BUILD_APK_WITH_GITHUB.txt and the included .github/workflows/build-apk.yml to build a debug APK in GitHub Actions without installing the Android SDK locally.
