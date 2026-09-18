#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

TOOLS="$(cd "$ROOT/.." && pwd)/.tools"
export JAVA_HOME="${JAVA_HOME:-$TOOLS/jdk-17.0.14+7}"
export ANDROID_HOME="${ANDROID_HOME:-$TOOLS/android-sdk}"

./gradlew :app:assembleRelease

SRC="$ROOT/app/build/outputs/apk/release/app-release.apk"
DEST_DIR="$ROOT/dist"
DEST="$DEST_DIR/RevLog.apk"

if [[ ! -f "$SRC" ]]; then
  echo "Release APK not found at $SRC" >&2
  exit 1
fi

mkdir -p "$DEST_DIR"
cp -f "$SRC" "$DEST"
echo "RevLog APK: $DEST"
