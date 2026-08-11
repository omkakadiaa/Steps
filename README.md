# Steps — Premium Step Counter for Samsung Galaxy S23 Ultra

A production-quality Android step counter with a dark, WHOOP-inspired fitness dashboard.

**Repo:** https://github.com/omkakadiaa/Steps

## Features

- Daily step tracking via `TYPE_STEP_COUNTER`
- Preloaded history: **Jan 1, 2026 → Aug 11, 2026** (223 days)
  - Every day **> 10,000** steps
  - All values **unique** and realistically varied
  - Labeled **DEMO DATA** vs **LIVE SENSOR**
- Home: ring progress, goal %, weekly/monthly/yearly averages, 14-day chart
- History: calendar, week strip, day list
- Insights: totals, trends, monthly averages, goal hit rate
- Offline-first Room DB · background tracking · boot + midnight handling
- Optimized for Galaxy S23 Ultra (AMOLED dark UI, large glanceable stats)

## Build APK (Android Studio)

1. Clone this repo and open it in Android Studio (SDK 34).
2. Let Gradle sync.
3. Build:

```bash
./gradlew assembleDebug
```

APK output:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install on S23 Ultra:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Rename to `Steps.apk` if you like.

### Publish a direct download link (GitHub Release)

1. Build the APK as above.
2. GitHub → **Releases** → **Create a new release**
3. Tag `v1.0.0`, attach `Steps.apk`
4. Direct link format:

```
https://github.com/omkakadiaa/Steps/releases/download/v1.0.0/Steps.apk
```

## Permissions

| Permission | Why |
|---|---|
| ACTIVITY_RECOGNITION | Step counter sensor |
| FOREGROUND_SERVICE / _HEALTH | Background tracking |
| POST_NOTIFICATIONS | Tracking notification (Android 13+) |
| RECEIVE_BOOT_COMPLETED | Resume after reboot |

All data stays on-device. No network required.

## Stack

Kotlin · Jetpack Compose · Material 3 · Room · SensorManager · minSdk 26 · targetSdk 34

## License

Personal / demo use.
