#!/usr/bin/env bash
# Release APK for arm64 only, named RevLog-<version>.apk from app/build.gradle.kts versionName.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

TOOLS="$(cd "$ROOT/.." && pwd)/.tools"
export JAVA_HOME="${JAVA_HOME:-$TOOLS/jdk-17.0.14+7}"
export ANDROID_HOME="${ANDROID_HOME:-$TOOLS/android-sdk}"

VERSION_NAME="$(grep 'versionName' "$ROOT/app/build.gradle.kts" | head -1 | sed -E 's/.*versionName = "([^"]+)".*/\1/')"
if [[ -z "$VERSION_NAME" ]]; then
  echo "Could not read versionName from app/build.gradle.kts" >&2
  exit 1
fi

./gradlew :app:assembleRelease

SRC="$ROOT/app/build/outputs/apk/release/app-release.apk"
DEST_DIR="$ROOT/dist"
DEST="$DEST_DIR/RevLog-${VERSION_NAME}.apk"

if [[ ! -f "$SRC" ]]; then
  echo "Release APK not found at $SRC" >&2
  exit 1
fi

mkdir -p "$DEST_DIR"
cp -f "$SRC" "$DEST"
echo "RevLog APK: $DEST"
