# Technical feasibility

Phase 0 findings, 2026-11-24. Each item is labelled **Verified (source)**, meaning confirmed by reading AOSP or Shizuku source, or **Unverified (device)**, meaning it needs a test on a physical device. No physical device was available while writing this.

Source used: `aosp-mirror/platform_frameworks_base`, branch `main`. The latest commit in that mirror is dated 2025-03-27, so the mirror lags current AOSP. Vendor builds such as HyperOS can differ from it.

## 1. Does the saturation operation exist? Verified (source)

`services/core/java/com/android/server/display/color/ColorDisplayShellCommand.java`:

```
usage: cmd color_display SUBCOMMAND [ARGS]
    set-saturation LEVEL   Sets the device saturation to the given LEVEL, 0-100 inclusive.
```

- LEVEL is parsed with `Integer.parseInt` and range-checked 0..100.
- It calls `ColorDisplayService.setSaturationLevelInternal(level)`.
- The same service is reachable over binder: `IColorDisplayManager.setSaturationLevel(int)` (hidden interface, service name `color_display`).
- The shell command was added in 2020, which is Android 11 (API 30). On Android 10 the binder method exists, but there is no `cmd` subcommand.

## 2. What privileges are required? Verified (source)

- `ColorDisplayService.BinderService.handleShellCommand` calls `enforceCallingOrSelfPermission(CONTROL_DISPLAY_COLOR_TRANSFORMS)`.
- `setSaturationLevel` (binder) accepts `CONTROL_DISPLAY_COLOR_TRANSFORMS` or the legacy `CONTROL_DISPLAY_SATURATION`.
- `isSaturationActivated` requires `CONTROL_DISPLAY_COLOR_TRANSFORMS`.
- `frameworks/base/packages/Shell/AndroidManifest.xml` grants `CONTROL_DISPLAY_COLOR_TRANSFORMS` to the shell UID (2000).

**Conclusion:** the operation needs shell-level privileges. Shizuku, started through wireless debugging, provides exactly that, and root is not required.

## 3. How does the system apply it? Verified (source)

`GlobalSaturationTintController.setMatrix(level)`:

- Level 100 means identity and the transform is deactivated.
- Below 100 it builds a 4×4 matrix with s = level/100 and d = 1 − s: `out = s·in + d·(0.231 R + 0.715 G + 0.072 B)`.
- The matrix is applied by SurfaceFlinger through `DisplayTransformManager` at `LEVEL_COLOR_MATRIX_SATURATION`. It is a real compositor-level transform, not an overlay.
- The change animates over the controller's transition duration, so it is not instantaneous.
- The transform is composed with the other display color matrices: Night Light, color correction, inversion, and Extra Dim / Reduce Bright Colors.
- GrayDial's in-app preview (`domain/ColorMath.kt`) uses the same coefficients.

`isAvailable()` for this controller returns `ColorDisplayManager.isColorTransformAccelerated(context)`. On devices without hardware-accelerated color transforms the matrix may not be applied, or may be applied in software. **Unverified (device).**

## 4. Execution mechanism chosen

| Option | Assessment |
|---|---|
| Shizuku `newProcess` running `sh -c` | Rejected. Deprecated, and private in API 13. It is also a general-purpose shell. |
| Hidden `IColorDisplayManager` called from the app process | **Rejected.** On the device, `IColorDisplayManager$Stub.asInterface` and its transaction fields are `api=blocked` on Android 16 for target SDK 37. |
| Shizuku UserService | **Built first, then rejected.** On HyperOS 3.0 (Android 16) with Shizuku 13.6.0, startup crashes in Shizuku's `ServiceStarter` with a `NullPointerException` in `LoadedApk.makeApplicationInner`, before any GrayDial code runs. GrayDial saw an 8 s bind timeout. |
| **Binder shell-command transaction through `ShizukuBinderWrapper` (chosen)** | Uses the same entry point as `adb shell cmd color_display set-saturation N`, with no process and no hidden methods. **Verified on the device:** about 1–2 ms per call; result code 0 for valid values and −1 for 150. |

State read-back uses the binder dump transaction on the same service and parses the `Global saturation: Activated` line. **Verified on the device.**

## 5. Persistence. Verified (source)

| Event | Effect on the saturation transform |
|---|---|
| GrayDial killed | **Persists.** State lives in `system_server`. |
| Shizuku stopped | **Persists**, but GrayDial can no longer change it until Shizuku is started again. |
| Screen lock/unlock | Persists (no code path resets it). **Unverified (device).** |
| User switch | Reset. `tearDown()` calls `setActivated(null)`. |
| Reboot | **Reset.** The level is held only in memory and never written to settings. |
| Another caller of `setSaturationLevel` (for example Digital Wellbeing Bedtime mode grayscale) | **Overwritten.** The last writer wins. |

GrayDial records `Settings.Global.BOOT_COUNT` with each successful change. On a mismatch it marks the stored state stale, switches the master toggle off, and shows a "reset by restart" notice with a Reapply button. It never reapplies the filter automatically.

## 6. Reading back the state

- Only `isSaturationActivated()` (boolean) is queryable. The exact level is not exposed by any API or by `dumpsys color_display`, which prints "Activated" only.
- GrayDial therefore distinguishes three cases:
  - **command succeeded**;
  - **system confirms filter on/off**;
  - **mismatch**, where another component overrode the filter.
- GrayDial never claims "verified at N%".

## 7. HyperOS 3.0 (Android 16) test device

- **Verified (device):**
  - `cmd color_display set-saturation` exists and works as the shell user, and rejects out-of-range values.
  - `dumpsys color_display` reports the `Global saturation: Activated` state, which matches each change.
  - Shizuku 13.6.0 starts over adb as `shell`, and GrayDial can be authorized.
  - GrayDial's in-app path applied 0, 25, 50, 75 and 100% and read each one back correctly. 40 rapid alternating calls all succeeded.
  - In the app UI, the Focus preset gave 75% and the system confirmed the filter was on. Restore gave normal colors and the system confirmed it was off.
- **HyperOS install restrictions:**
  - `adb install` was refused once with `INSTALL_FAILED_USER_RESTRICTED` and succeeded on retry.
  - Gradle's `connectedAndroidTest` fails because it installs with `-g`, which needs "USB debugging (Security settings)". Installing the APKs manually and running `am instrument` works.
- **Not verified:**
  - How the screen looks to the eye in each app. Screenshots don't show the effect.
  - HDR, DRM video and HyperOS color modes.
  - Whether Shizuku survives turning wireless debugging off.
  - Behaviour after a reboot.

## 8. Minimum Android version

**API 30 (Android 11).** This is when on-device wireless debugging (pairing without a computer) and the `cmd color_display` subcommand were introduced. The binder method is older, but there is no computer-free way to start Shizuku before Android 11.

## 9. Security concerns

- Shizuku grants authorized apps full shell (UID 2000) capability, which is far broader than GrayDial needs. GrayDial cannot narrow what Shizuku can do. It can only narrow what GrayDial's own code exposes. See SECURITY.md.
- Hidden-API reflection could break on future Android versions. The failure mode is safe: GrayDial falls back to `cmd`, and if that also fails it reports "Unsupported". Nothing is applied silently.
