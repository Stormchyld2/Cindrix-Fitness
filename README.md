# Cindrix Fitness

Offline Android app built around the Hybrid Muscle, Fat Loss and Boxing program: three training days a week (Monday, Wednesday, Friday) with band strength work, slam ball and boxing, plus nutrition and tracking. Part of the Cindrix line (Cindrix Development).

## Status

Planning and design. No app code yet. Mockups now show the exercise how-to pop-up, trackers and rewards.

## What is here

| Path | What it is |
| --- | --- |
| `docs/hybrid-guide.docx` | The program the app is built on (revision 2, 2026-11-20). See its revision notes for what changed. |
| `docs/archive/band-program-superseded.md` | An earlier general band program. Superseded by the Hybrid guide; it assumed no joint limits and a different anchor, so it is not used. |
| `design/mockups/` | Seven phone screens (Today, Program, Progress, Workout A, Active set, Library, Exercise detail) as `.dc.html` artboards plus `canvas.json`. Sample data and the earlier band program's content, to be updated to the Hybrid guide. |
| `design/reference/chatgpt-style.jpg` | The style reference the mockups follow (dark navy, glowing blue, hero banners, 5-tab bar). |
| `design/reference/guide-cards/` | The Monday, Wednesday and Friday exercise cards from the guide. They still show 30-second rest, and the Friday card still shows the bar curl. |
| `assets/logo.png` | Current Cindrix Fitness logo. |

## Plan

- Native Android, Kotlin and Jetpack Compose, fully offline.
- Program data bundled as app data: workouts, bands, rings, progression rules, boxing and slam-ball rounds.
- Today, Program, Workouts, Progress and Library tabs, with an exercise chart for each movement.
- Round timer for slam ball and boxing, set and band log, joint and pain tracker, weight and measurement tracking.
- Original exercise illustrations for the app. Demo videos open online when connected.
- APK named `CindrixFitness-[version]-android.apk`, built by GitHub Actions.

## Branches

Work happens on `dev`. `main` is updated only when the owner says so.

© 2026 Cindrix Development
