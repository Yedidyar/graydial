# Architecture

GrayDial is a single-module app with no navigation library and no DI framework.

```
UI (Compose)                    Quick Settings tile
  GrayDialScreen                  GrayDialTileService
  GrayDialViewModel ──┐               │
                      ▼               ▼
               domain/GrayDialController   ◄── single source of truth for intent → operation
                 │            │
   data/PreferencesRepository  domain/SaturationController (interface)
   (DataStore, local only)            │
                                      ▼
                       system/ShizukuSaturationController
                                      │ ensureReady()
                       system/ShizukuConnectionManager ── Shizuku binder + authorization state
                                      │
                       system/ShizukuColorDisplay ── the only privileged code
                         ├─ set: binder shell-command transaction on `color_display`
                         │       args = ["set-saturation", <validated Int>]
                         └─ read: binder dump of `color_display` → "Global saturation: Activated"
                                      │ via ShizukuBinderWrapper (call runs as shell UID)
                     ─ ─ ─ ─ ─ system_server: ColorDisplayService (re-validates 0..100) ─ ─ ─ ─ ─
```

## State separation

| Concept | Where | Notes |
|---|---|---|
| Requested intensity | `GrayDialPreferences.intensity` | The user's preference. It is kept when grayscale is switched off. |
| Requested on/off | `GrayDialPreferences.enabled` | Rolled back if applying the change fails. |
| Last successfully applied saturation | `GrayDialPreferences.lastAppliedSaturation` (+ boot count, timestamp) | Written **only** after the system accepted the value. |
| Actual system state | `SaturationController.isSaturationActivated()` | Boolean only. Reported as `Verification.CONFIRMED`, `MISMATCH` or `UNAVAILABLE`. |
| Permission/connection state | `ShizukuConnectionManager.state` | Resolved by the pure `PermissionStateResolver`. |
| Transient slider position | `GrayDialViewModel.sliderOverride` | Gives instant UI feedback before persistence catches up. |

## Key decisions

- **One controller for UI and tile.** `GrayDialController` serializes all operations with a mutex. The UI and the tile therefore cannot disagree or race.
- **Debounced slider.** `LatestValueApplier` debounces at 120 ms and conflates input. The system receives near-live updates, but never more than one in-flight call. Calls are never cancelled mid-flight, so a successful change is always recorded.
- **No redundant privileged calls.** If the target equals the last applied value in the same boot, the controller returns `Unchanged`. "Restore original colors" always sends the command anyway, because it is the recovery path.
- **No privileged process of our own.** Each operation is one binder call routed through Shizuku (~1–2 ms on the HyperOS 3.0 test device). An earlier design used a Shizuku UserService, but Shizuku 13.6.0 crashes while starting user services on Android 16 / HyperOS 3.0 (`LoadedApk.makeApplicationInner` NPE), so it was removed.
- **No automatic reapply after reboot.** There is no `BOOT_COMPLETED` receiver. A boot-count mismatch marks the state stale and shows a notice.
- **Tile is conservative.** The tile shows only the last applied state. While a change runs it shows "Applying…". Afterwards it asks the system to re-query (`requestListeningState`).
- **Spectrum preview** uses the exact AOSP saturation matrix (`ColorMath`).

## Setup wizard

`SetupWizard` (pure, unit-tested) maps `PermissionState` to one of Install / Start / Grant / Reconnect / Unsupported / Done and to the single button action of that step. `SetupWizardCard` renders it and shrinks to a one-line "Ready" when done. Launcher shortcuts go through `ShortcutActivity`, which runs the same `GrayDialController` calls as the tile.

## Package layout

```
com.graydial.android
  MainActivity, GrayDialApplication (AppContainer)
  domain/   GrayscaleIntensity, Preset, SaturationController, SaturationException,
            PermissionState(+Resolver), SetupWizard, GrayDialController, LatestValueApplier, ColorMath
  data/     PreferencesRepository
  system/   ShizukuConnectionManager, ShizukuSaturationController, ShizukuColorDisplay
  ShortcutActivity (invisible launcher-shortcut target)
  tile/     GrayDialTileService
  ui/       GrayDialScreen, GrayDialViewModel, GrayDialUiState, components/, theme/
```
