# Cindrix Fitness

Offline Android app built around a 12-week tension band workout program. Part of the Cindrix line (Cindrix Development).

## Status

Planning and design. No app code yet.

## What is here

| Path | What it is |
| --- | --- |
| `docs/program.md` | The full 12-week program: setup, workouts A and B, progression, tests, band care. App content comes from this file. |
| `design/mockups/` | Seven phone screens (Today, Program, Progress, Workout A, Active set, Library, Exercise detail) as `.dc.html` artboards, plus `canvas.json` for layout. Sample data only. |
| `design/reference/chatgpt-style.jpg` | The style reference the mockups follow (dark navy, glowing blue, hero banners, 5-tab bar). |
| `assets/logo.png` | Current Cindrix Fitness logo. |

## Plan

- Native Android, Kotlin and Jetpack Compose, fully offline.
- Program data (weeks, workouts, bands, rings) bundled as app data.
- Today, Program, Workouts, Progress and Library tabs.
- Workout timer with tempo cues, set and band log, week 1 and week 12 tests, band-up prompt.
- Original exercise illustrations drawn for the app. Demo videos open online when connected.
- APK named `CindrixFitness-[version]-android.apk`, built by GitHub Actions.

## Branches

Work happens on `dev`. `main` is updated only when the owner says so.

© 2026 Cindrix Development
