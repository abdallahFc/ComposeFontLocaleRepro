# Compose Font Locale Repro

Minimal Android repro for a possible Jetpack Compose `ResourceFont` cache issue after runtime app locale changes.

Open a new Android Issue Tracker bug in the Jetpack Compose component:

<https://issuetracker.google.com/issues/new?component=612128>

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

## Android Issue Tracker Draft

Use this text when filing the bug:

```text
Title:
Compose ResourceFont cache is not invalidated after locale change when the same R.font id resolves to locale-qualified font resource

Jetpack Compose version:
Compose BOM 2026.02.01
androidx.compose.ui:ui / ui-text resolved by the BOM: 1.10.4

Jetpack Compose component used:
Text / ui-text / FontFamily / ResourceFont

Android Studio Build:
Android Studio 2025.3 — AI-253.31033.145.2533.15113396

Kotlin version:
2.2.10

Devices/Android versions reproduced on:

* [Device name or Emulator] — Android [API level]
* [Another device if tested]

Keyboard:
N/A — this issue is not related to keyboard input.

Summary:
When a Compose Text uses FontFamily(Font(R.font.locale_test_font)), and the same font resource has locale-qualified alternatives such as res/font/locale_test_font.ttf and res/font-ar/locale_test_font.ttf, changing the app language without killing the process can keep rendering with the previously cached Typeface.

The string resource updates correctly after Activity recreation, but the font can remain stale until the process is killed or the app is cold-started again.

Steps to reproduce:

1. Open the attached sample project.
2. Run the app in English.
3. Confirm the screen shows:

   * current language
   * current locale
   * current process id
   * Compose Text using FontFamily(Font(R.font.locale_test_font))
   * localized stringResource text
4. Tap "Switch to Arabic".
5. Confirm the process id stays the same.
6. Confirm the stringResource text changes to Arabic.
7. Observe the Compose Text font.
8. Tap "Switch to English".
9. Observe the Compose Text font again.
10. Force stop the app and reopen it.
11. Observe that the correct font is used after cold start.

Expected behavior:
Compose should resolve the ResourceFont again using the current locale/configuration after app language change, or the font cache key should change when configuration-qualified resources can resolve to a different font file.

If R.font.locale_test_font resolves to res/font-ar/locale_test_font.ttf after switching to Arabic, Compose Text should render using the Arabic font without requiring a process restart.

Actual behavior:
Compose can keep using the Typeface cached before the language change. stringResource updates correctly, but the font may remain from the previous locale until the app process is killed and started again.

Why this matters:
This makes dynamic app language switching inconsistent. XML views can re-inflate and resolve locale-qualified font resources again after Activity recreation, while Compose Text may reuse a stale cached Typeface for the same R.font id.

Sample project:
[Attach ZIP or add public GitHub repo link]

Screenrecord / screenshots:
[Attach screenrecord showing locale changes, same process id, stringResource changes, and font remaining stale]

Stack trace:
N/A — no crash.

Workaround:
Selecting explicit different font resources per language from LocalConfiguration.current works around the issue, for example using a different FontFamily for Arabic and English. However, the issue is specifically about ResourceFont caching when the same R.font id resolves to different locale-qualified font files.

Additional notes:
The issue is hidden if the app kills the process during language change because all font caches are cleared. It becomes visible when language switching is handled by updating locale and recreating Activity without process restart.
```
