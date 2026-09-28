# Status Saver

A simple, clean WhatsApp status saver for Android, written in Kotlin.

## What it does

- Scans a folder you choose for images/videos and shows them in two tabs (Images, Videos).
- Tap the download button on any thumbnail (or open it full-screen and tap the FAB) to copy it
  to a folder you choose.
- Both the **source** folder (where WhatsApp keeps statuses) and the **destination** folder
  (where saved files go) are configurable from Settings — nothing is hardcoded.

## Why it uses folder pickers instead of a hardcoded path

Since Android 10/11 (scoped storage), apps can no longer read arbitrary paths like
`storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/.Statuses/` using plain
`File` APIs — that path lives inside another app's `Android/media` directory. The
Storage Access Framework (SAF) is the supported way in: the user grants a specific
folder via the system picker, and the app gets a persistable URI permission to that
folder (survives reboots) without needing `READ_EXTERNAL_STORAGE` or
`MANAGE_EXTERNAL_STORAGE` at all. That's why the manifest has **no storage permissions**
declared — it isn't needed.

This is exactly how all the WhatsApp status saver apps on the Play Store work today.

## First run

1. Install and open the app — it will show "No statuses found" since nothing is configured yet.
2. Tap the settings (gear) icon in the toolbar.
3. Under **WhatsApp Status Folder**, tap **Choose Folder**. The picker will try to open
   directly at `Android/media/com.whatsapp/WhatsApp/Media/.Statuses` — just tap **Use this
   folder**. If your device's picker doesn't honor that hint, navigate there manually
   (enable "Show hidden files" in the picker's overflow menu if `.Statuses` isn't visible).
   Using WhatsApp Business instead? The folder is under `com.whatsapp.w4b` instead of `com.whatsapp`.
4. Under **Save Location**, tap **Choose Folder** and pick (or create) wherever you want
   saved statuses to land, e.g. `Pictures/StatusSaver`.
5. Go back — your statuses now show up in the grid. Tap the download icon to save one.

## Project structure

```
app/src/main/java/com/example/statussaver/
  MainActivity.kt          Tabs (Images/Videos) + settings entry point
  SettingsActivity.kt       Folder pickers for source & destination (SAF)
  PreviewActivity.kt        Full-screen image/video preview with a save FAB
  StatusScanner.kt          Lists files in the source folder via DocumentFile
  SaveHelper.kt              Copies a file from source URI to destination folder
  StatusAdapter.kt           RecyclerView grid adapter
  StatusListFragment.kt      One tab's grid + pull-to-refresh + empty state
  StatusPagerAdapter.kt      ViewPager2 adapter (2 pages: images, videos)
  StatusItem.kt               Data model
  Prefs.kt                     Stores the two configured folder URIs
```

## Building

1. Open this folder (`StatusSaver/`) directly in Android Studio (Hedgehog or newer
   recommended). It doesn't include a Gradle wrapper — when Android Studio opens it,
   accept the prompt to generate one, or run `gradle wrapper` once if you have Gradle
   installed locally.
2. Let Gradle sync (it will download AndroidX, Material Components, Glide, and
   Kotlin coroutines — all from Google/Maven Central).
3. Run on a device or emulator running Android 7.0 (API 24) or newer.

## Notes / things you may want to extend

- Currently recognizes `.jpg/.jpeg/.png/.webp` as images and `.mp4/.3gp/.mkv` as videos —
  add extensions in `StatusScanner.kt` if needed.
- No "already viewed vs new" distinction is made — it just lists whatever is currently in
  the source folder (which is what WhatsApp keeps there day-to-day).
- Saving skips a file if one with the same name already exists at the destination — you
  can change that behavior in `SaveHelper.kt`.
- If the user later revokes folder access (rare, but possible after a device restore),
  `SaveHelper` and `StatusScanner` will surface a clear error rather than crash — just
  have them reselect the folder in Settings.
