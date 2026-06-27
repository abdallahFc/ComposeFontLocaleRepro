# Compose Font Locale Repro

Minimal Android repro for a possible Jetpack Compose `ResourceFont` cache issue after runtime app locale changes.

Filed Android Issue Tracker bug:

<https://issuetracker.google.com/issues/528500167>

Screen recording:

https://github.com/user-attachments/assets/c76632d7-6eac-4636-ba15-4a0f988efaff


## Problem

When Compose `Text` uses `FontFamily(Font(R.font.locale_test_font))`, and the same font resource has locale-qualified alternatives under `res/font` and `res/font-ar`, changing app language without killing the process may keep rendering with the previously cached `Typeface`.

`stringResource` updates correctly after Activity recreation, but the font may remain stale until process restart.

## Why this sample exists

The main repro path deliberately uses the same font resource id in all locales:

```kotlin
FontFamily(Font(R.font.locale_test_font))
```

The same resource name exists in two resource folders:

```text
app/src/main/res/font/locale_test_font.ttf
app/src/main/res/font-ar/locale_test_font.ttf
```

The optional **Workaround control** is separate. It explicitly switches between different resource ids based on `LocalConfiguration.current`:

```text
app/src/main/res/font/locale_test_font_en_explicit.ttf
app/src/main/res/font/locale_test_font_ar_explicit.ttf
```

That control is only present to show that changing the `FontFamily` key can force a different font. It is not the main repro path.

## How to run

1. Open the project in Android Studio.
2. Build and install the `app` debug variant on a device or emulator.
3. Watch logcat with tag `FontLocaleRepro`.

## Steps to reproduce

1. Launch the app in English. If it opens in Arabic because of prior app locale state, tap **Switch to English** first.
2. Confirm the default English font is used.
3. Tap **Switch to Arabic**.
4. Confirm the process id stays the same.
5. Confirm `stringResource` text changes to Arabic.
6. Observe whether **Compose text using ResourceFont** changes to the Arabic font or keeps the previous cached English `Typeface`.
7. Tap **Switch to English**.
8. Observe whether **Compose text using ResourceFont** changes back or keeps the previous cached Arabic `Typeface`.
9. Force stop the app and reopen it.
10. Confirm the correct font is used after cold start.

## Expected behavior

After `AppCompatDelegate.setApplicationLocales(...)` changes the app language, `FontFamily(Font(R.font.locale_test_font))` should resolve the locale-qualified font for the current app locale while the process id remains unchanged.

If `R.font.locale_test_font` resolves to `res/font-ar/locale_test_font.ttf` after switching to Arabic, Compose `Text` should render using the Arabic font without requiring a process restart.

## Actual behavior

Record the behavior observed on the test device. On affected builds, `stringResource` changes to the new locale, but the Compose `Text` using `R.font.locale_test_font` may keep rendering with the previously cached `Typeface` until the app process is killed and started again.

## Device / Android version

Fill in the device model and Android version used for the issue report.

```text
- [Device name or Emulator] — Android [API level]
- [Another device if tested]
```

## Compose version

Compose BOM: `2026.02.01`

Compose UI resolved by the BOM in this sample: `androidx.compose.ui:ui` / `ui-text` `1.10.4`.

## Kotlin version

Kotlin: `2.2.10`

## Android Studio version

Android Studio: `2025.3`

Build: `AI-253.31033.145.2533.15113396`

## Notes for Android Issue Tracker

- Main repro font: `FontFamily(Font(R.font.locale_test_font))`
- Default font resource: `app/src/main/res/font/locale_test_font.ttf`
- Arabic font resource: `app/src/main/res/font-ar/locale_test_font.ttf`
- Locale switching: `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en" | "ar"))`
- The Activity may recreate, but the process should not be killed.
- The screen displays `android.os.Process.myPid()` for verification.
- Logcat tag: `FontLocaleRepro`
- The optional explicit control uses `R.font.locale_test_font_en_explicit` and `R.font.locale_test_font_ar_explicit`; it is not the main repro path.
