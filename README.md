# Java Interview Prep

A minimalist, developer-native Android app for Java interview preparation. Fully offline with bundled JSON content, lightweight dev-flavored gamification, and spaced repetition.

## Architecture & Modules

```
├── :core-model      Pure Kotlin: Content models, sealed Exercise types, SRS & Gamification domain
├── :core-engine     Android Library: Room DB, repositories, SRS engine, XP/streak logic, swappable ThemePack & design tokens
├── :content-java    Android Library: Bundled JSON assets at src/main/assets/packs/java/
└── :app             Android Application: Application entry point, Hilt wiring, UI screens
```

## Prerequisites (Linux)

- **JDK 17 or 21** (e.g. OpenJDK 21):
  ```bash
  sudo apt install openjdk-21-jdk
  ```
- **Android SDK** (API 35+ / API 37, Build Tools):
  Ensure `local.properties` contains your SDK directory or `ANDROID_HOME` is exported:
  ```bash
  echo "sdk.dir=$HOME/Android/Sdk" > local.properties
  ```

## Building & Testing

### 1. Build Debug APK
```bash
./gradlew assembleDebug
```
The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

### 2. Run All Unit Tests
```bash
./gradlew test
```

### 3. Install and Launch on Emulator/Device
Ensure an Android device or emulator is running (`adb devices`):
```bash
./gradlew installDebug
adb shell am start -n io.github.ducthinh0304.javainterviewprep/.MainActivity
```

## Design Philosophy

- **Developer-Native Aesthetics**: Deep obsidian surfaces, hairline borders (1dp), zero elevation shadows, monospace badges for code and status.
- **Swappable Theme Packs**: Design tokens (colors, typography, shapes, spacing, copy tone) live in a swappable `ThemePack` interface.
- **Offline First**: All questions, lessons, and metadata are bundled in offline JSON packs. No account or network connection required.
- **Dev Gamification**: Level progression follows software releases: `SNAPSHOT` → `ALPHA` → `BETA` → `RC` → `GA` → `LTS` with XP and combo multiplier.
