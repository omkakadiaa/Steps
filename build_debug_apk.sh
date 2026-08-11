#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ ! -f gradlew ]]; then
  echo "Open once in Android Studio to generate the Gradle wrapper, or run: gradle wrapper --gradle-version 8.7"
  exit 1
fi
./gradlew assembleDebug
echo "APK: $(pwd)/app/build/outputs/apk/debug/app-debug.apk"
