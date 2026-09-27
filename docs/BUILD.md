# Building RevLog

Requirements: **JDK 17**, Android SDK (see `local.properties` for `sdk.dir`).

## Standard toolchain

Create `local.properties` (not committed):

```properties
sdk.dir=C\:\\path\\to\\android-sdk
```

```bash
./gradlew assembleDebug
./scripts/check.sh
```

## Private workspace toolchain

```bash
export JAVA_HOME="../.tools/jdk-17.0.14+7"
export ANDROID_HOME="../.tools/android-sdk"
```

## Release APK (arm64)

**Prerequisite:** `keystore/release.keystore` (not committed).

```bash
mkdir -p keystore
keytool -genkeypair -v -keystore keystore/release.keystore -alias revlog \
  -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android \
  -dname "CN=RevLog, OU=Dev, O=RevLog, L=Local, ST=Local, C=AT"

bash scripts/build_apk.sh
```

APK: **`dist/RevLog-<version>.apk`**
