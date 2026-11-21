# Cindrix Fitness: app plan (rev 2)

Offline Android app (Kotlin + Jetpack Compose). Built from the Hybrid Muscle, Fat Loss and Boxing guide, but customizable.

## Direction (decided 2026-11-20)

- **Workouts: the guide is a template you can edit.** The app starts with the Mon/Wed/Fri Hybrid program. Any exercise can be swapped from the library, added, or removed, and extra routines can be built. Joint-pain substitutes still work.
- **Different exercises across the three days.** The guide repeats squat, chest press and triceps pressdown on Wednesday/Friday. The default plan should not repeat movements across days. Proposed Friday variations come from the guide's own substitutes: box squat, single-arm chest press, overhead triceps extension (only if the shoulder is comfortable). Confirm each swap before it becomes the default.
- **Nutrition: tracker plus Mediterranean guide.** Log calories, protein and water against the guide's targets (about 2,400 kcal, about 190 g protein). Add a Mediterranean food guide, meal ideas and a shopping list layered on top of those targets, so protein is not lost.

## Screens

Today, Program (editable), Workouts, Exercise pop-up (photo plus how-to), Active set, Progress, Trackers, Joint and pain, Rank and rewards, Library (searchable, filterable), Nutrition (log, Mediterranean guide, shopping list).

## Exercise library

- Have photos now (16, approved): chest press, seated row, lat pulldown, triceps pressdown, weighted-bar curl, band-bar squat, pull-through, hamstring curl, glute kickback, Pallof press, band-bar overhead press, face pull, overhead slam, heavy bag, electronic boxing machine, shadowboxing warm-up.
- Needed for the guide itself: dead bug (core finisher option), 4 cooldown stretches (chest, lat, hip flexor, hamstring).
- Needed for the guide's yellow-zone substitutes: single-arm chest press, standing row, high band row, single-arm pressdown, seated handle curl, box squat, standing glute extension, standing hamstring curl, single-arm shoulder press, reverse fly, overhead triceps extension.
- Extras for a bigger library (not in the guide, labelled as such): to be chosen. Each needs a matching photo (same model, same door strap, plain "C" clothing, no brand logos).

## Rules the app must keep

- Joint and pain system (green, yellow, red) with the guide's substitute movements.
- Rewards never pay for training through pain. Streaks count weeks, not days.
- Slams are straight overhead or chest-height only. No twisting or jumping variants.
- No medical claims. The guide is educational.

## Build

Kotlin + Compose, fully offline, program and library data bundled. GitHub Actions builds `CindrixFitness-[version]-android.apk`. Work stays on `dev` until told to merge to `main`. Not started; waiting on "go".
