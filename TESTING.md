# Testing

## Results

| Check | Command | Result |
|---|---|---|
| Unit tests (JVM) | `./gradlew testDebugUnitTest` | **57 passed, 0 failed** |
| Android lint | `./gradlew lintDebug` | **No issues found** |
| Debug / release APK | `./gradlew assembleDebug assembleRelease` | Built (release ~2.1 MB, minified, unsigned) |
| Instrumented tests on **HyperOS 3.0 / Android 16 device** (Shizuku 13.6.0) | see below | **5 passed, 0 failed, 0 skipped** |
| In-app check on the device | adb-driven taps | The Focus preset applied 75% and the system confirmed the filter was on. Restore gave normal colors and the system confirmed it was off. |

On HyperOS, Gradle's `connectedDebugAndroidTest` can't install the test APK unless "USB debugging (Security settings)" is on, because Gradle installs with `-g`. Run the tests manually:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.graydial.android.test/androidx.test.runner.AndroidJUnitRunner
```

The automated checks verify the system state the device reports. They can't show what the screen looks like: screenshots don't include the color transform, so visual checks need a person.

## Phase 1 proof of concept

No separate proof-of-concept APK was built. The MVP already covers the proof of concept's scope:

- Shizuku permission handling.
- **Original** and **Restore original colors**, which send saturation 100.
- The **Balanced (50%)** and **Monochrome (100%)** presets.
- The "System state" result line.

Running the Setup and Core sections below is the proof-of-concept validation.

## Unit test coverage (`app/src/test`)

| Suite | Covers |
|---|---|
| `GrayscaleIntensityTest` | Grayscale-to-saturation conversion, all 101 round trips, boundaries, invalid input, preset values |
| `ColorMathTest` | Preview matrix matches AOSP coefficients, identity at 100, gray at 0 |
| `PermissionStateResolverTest` | All 8 permission/connection states, revocation, Sui (binder without visible package), pre-v11 |
| `PreferencesRepositoryTest` | Persistence (real DataStore on a temp file), defaults, invalid input, corrupt values, restart reset |
| `GrayDialControllerTest` | Master switch, intensity while off/on, deduplication, failure rollback, restore-always-sends, presets, tile toggle uses applied state, reboot handling, verification (confirmed/mismatch/unavailable), recovery after failure |
| `LatestValueApplierTest` | Debouncing and conflation with virtual time |
| `GrayDialUiStateTest` | Command success is never described as verified; pending-change detection |

## Instrumented tests (`app/src/androidTest`)

| Test | Requires | Device result |
|---|---|---|
| `ColorDisplayValidationTest` | Any device | Passed. Out-of-range values are rejected before any binder call. |
| `ShizukuIntegrationTest.appliesAndRestoresSaturation` | Shizuku running and GrayDial authorized | Passed |
| `ShizukuIntegrationTest.everyPresetLevelIsAcceptedAndReadBack` | Shizuku running and GrayDial authorized | Passed. Covers 100, 75, 50, 25, 0 and 100; each read-back matched. |
| `ShizukuIntegrationTest.repeatedRapidCallsAllSucceed` | Shizuku running and GrayDial authorized | Passed (40 calls) |
| `ShizukuIntegrationTest.appLayerRejectsInvalidValuesWithoutCallingService` | Shizuku running and GrayDial authorized | Passed |


## Physical-device acceptance checklist (HyperOS)

Record the result of each item, with HyperOS and Android versions and Shizuku version.

### Setup

- [ ] Install the debug APK: `adb install app/build/outputs/apk/debug/app-debug.apk`, or sideload it.
- [ ] Install Shizuku and start it via Wireless debugging (no computer).
- [ ] Open GrayDial. The status reads "Access required". Tap **Grant access**, then allow it. The status reads "Ready".
- [ ] Deny once, then retry. "Access denied" is shown and recoverable.

### Core

Set each of 0%, 25%, 50%, 75% and 100%. Confirm the effect looks correct on each of these surfaces:

- [ ] Home screen
- [ ] Settings
- [ ] Chrome
- [ ] WhatsApp
- [ ] Google Maps
- [ ] Waze
- [ ] Notification shade
- [ ] Quick Settings

Then check:

- [ ] The "System state" line says the system confirms the filter is on or off. If it says the state "could not be read back", note it.
- [ ] Dragging the slider updates the screen while dragging (debounced) without lag or flicker.
- [ ] Turn the master switch off: colors are restored and the slider keeps its value. Turn it on: the previous value is reapplied.
- [ ] Presets apply. "Original" restores colors.
- [ ] **Restore original colors** works from every state.

### Quick Settings tile

- [ ] Add the tile. It shows "Off" or "N% gray" correctly.
- [ ] Tapping toggles it, with "Applying…" shown until the change completes.
- [ ] Long-pressing opens GrayDial.
- [ ] With Shizuku stopped, a tap shows "Setup needed" and the state does not flip.

### Lifecycle

- [ ] Lock and unlock the screen: the filter persists.
- [ ] Force-stop GrayDial: the filter persists, and on reopen the state is shown correctly.
- [ ] Stop Shizuku: the filter persists, GrayDial shows "Shizuku is not running", and "Restore" reports a clear error.
- [ ] Turn Wireless debugging off after Shizuku has started. **Does Shizuku keep running? Record this.**
- [ ] Reboot: colors are normal, GrayDial shows "reset by a device restart", and **Reapply** works once Shizuku is restarted.

### Interactions

- [ ] Developer options › Simulate color space › Monochromacy, combined with GrayDial.
- [ ] Accessibility › Color correction (grayscale) and Color inversion, each combined with GrayDial.
- [ ] Digital Wellbeing / Bedtime grayscale: expect last-writer-wins; GrayDial should show a mismatch.
- [ ] HyperOS Display › Color scheme (Vivid/Saturated/Standard/Advanced), Reading mode, and Night Light.
- [ ] HDR video (YouTube HDR), video playback, and DRM/secure surfaces (Netflix): note whether they are desaturated.
- [ ] Screenshots: note whether captures are gray. The system matrix is usually not captured.
