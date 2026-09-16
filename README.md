# RevLog

Personal vehicle logbook for cars and motorcycles. Track specs (Daten) and service history (Service), export/import `.revlog` files for backup or sharing.

## Stack

- Kotlin, Jetpack Compose, Material 3
- Hilt, Room, Navigation Compose
- Modules: `app`, `core-domain`, `core-data`

## Build

Create `local.properties` (not committed) pointing at your Android SDK, e.g.:

```properties
sdk.dir=C\:\\path\\to\\android-sdk
```

```bash
export JAVA_HOME="../.tools/jdk-17.0.14+7"
export ANDROID_HOME="../.tools/android-sdk"
./gradlew assembleDebug
./scripts/check.sh
```

### Release APK (arm64 only)

**Prerequisite:** `keystore/release.keystore` must exist (not committed). Create once:

```bash
mkdir -p keystore
keytool -genkeypair -v -keystore keystore/release.keystore -alias revlog \
  -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android \
  -dname "CN=RevLog, OU=Dev, O=RevLog, L=Local, ST=Local, C=AT"
```

```bash
./gradlew :app:assembleRelease
```

APK output: `app/build/outputs/apk/release/app-release.apk`

## Export format

Files use extension `.revlog` (JSON, versioned). See `core-data/.../RevLogBackupManager.kt`.

## Localization

- Default: German (`values-de-rAT`)
- Infrastructure for `en-GB` and `es-ES` prepared

## Future

Maintenance reminders are documented in [docs/reminders.md](docs/reminders.md).
