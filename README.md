# GrayDial

**Less color. More focus.**

GrayDial reduces the color saturation of your entire Android display gradually, from 0% (full color) to 100% (monochrome). Android's built-in grayscale is all-or-nothing; GrayDial gives you everything in between.

It uses Android's own compositor color pipeline (`ColorDisplayService` global saturation). It does not use overlays, screen capture, or accessibility services. Root is not required; GrayDial uses [Shizuku](https://github.com/RikkaApps/Shizuku), which you start on-device with Wireless debugging.

> **Status:** MVP, unreleased. The core path works on a **HyperOS 3.0 (Android 16) device with Shizuku 13.6.0**: the system confirms each level is applied and that Restore works. The visual checks, tile, reboot and app-interaction checks are still open. See [TESTING.md](TESTING.md).

## Features

- Intensity slider covering every integer from 0 to 100%, with debounced live updates.
- Master switch. Turning it off restores normal colors and keeps your chosen intensity.
- Presets: Original 0%, Subtle 25%, Balanced 50%, Focus 75%, Monochrome 100%.
- Your own presets. Move the slider to any level, tap **+ Save N%** to add it next to the built-ins (up to 8), and tap **×** to remove one.
- A spectrum preview computed with the same matrix Android applies.
- A Quick Settings tile. Tap to toggle; long-press to open the app.
- A **Restore original colors** button that is always visible.
- Honest status reporting. GrayDial distinguishes three cases:
  - the command succeeded;
  - the system confirmed the filter is on or off;
  - something else overrode the filter.
- No internet permission, no analytics, no accounts.

## Requirements

- Android 11 (API 30) or newer. Wireless debugging is required to start Shizuku without a computer.
- [Shizuku](https://shizuku.rikka.app/) v11 or newer. v13.x is recommended.
- The primary target is Xiaomi HyperOS. Pixel, Samsung and other AOSP-based devices are expected to work, but this is unverified.

## Install

1. Build the app (see below) or get a signed APK from the owner.
2. Install it: `adb install app-debug.apk`, or open the APK on the phone.

## Set up Shizuku (on the phone, no computer)

1. Install Shizuku from Google Play or GitHub.
2. Connect to Wi-Fi.
3. Enable Developer options. On Xiaomi: Settings › About phone › tap **OS version** 7 times.
4. Open Settings › Additional settings › Developer options and turn on **Wireless debugging**.
5. In Shizuku, choose **Start via Wireless debugging › Pairing**. Then go to Developer options › Wireless debugging › **Pair device with pairing code** and enter the code into Shizuku's notification.
6. Tap **Start** in Shizuku.
7. Open GrayDial, tap **Grant access**, and allow it.

After Shizuku is running you can usually turn Wireless debugging off again. Do **not** turn off Developer options; on many ROMs that stops Shizuku. Whether Shizuku survives turning off Wireless debugging on your specific device is not yet confirmed.

Shizuku stops on every reboot. Repeat step 6 after restarting.

## Permissions

GrayDial itself requests no dangerous or network permissions. It needs exactly one thing: **Shizuku authorization**, which you grant in Shizuku's dialog. Shizuku's shell-level access is broader than what GrayDial uses; see [SECURITY.md](SECURITY.md).

## Restore normal colors

Use any one of these:

- Tap **Restore original colors** in GrayDial.
- Turn off the GrayDial Quick Settings tile.
- **Restart the phone.** Android always resets the saturation at boot, so this works even if GrayDial or Shizuku is broken.
- With a computer: `adb shell cmd color_display set-saturation 100`.

## Troubleshooting

| Message | Fix |
|---|---|
| Shizuku not installed | Install Shizuku. |
| Shizuku is not running | Open Shizuku and start it. This is needed after every reboot. |
| Access required / denied | Tap **Grant access**. If no dialog appears, open Shizuku › Authorized applications and enable GrayDial. |
| Connection lost | Shizuku stopped responding. Open Shizuku, make sure it's running, then tap **Reconnect**. |
| Not supported | The device or vendor build rejects the system saturation control. Update Shizuku. Report the HyperOS version. |
| "The system reports a different filter state" | Another feature (Bedtime mode, a reading mode, another app) changed saturation. Re-apply in GrayDial. |
| Colors reset after a restart | This is expected. Tap **Reapply** once Shizuku is running. |

## Build locally

You need JDK 17+ (JDK 21 tested) and the Android SDK with platform `android-37.2` and build-tools 37.

```bash
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties
./gradlew assembleDebug          # app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest lintDebug
./gradlew assembleRelease        # unsigned, minified; sign it outside the repo
```

Dependency verification is on. If you change dependencies, run:

```bash
./gradlew --write-verification-metadata sha256 assembleDebug assembleRelease testDebugUnitTest lintDebug assembleDebugAndroidTest
./gradlew :app:dependencies --write-locks
```

## Known limitations

- Shizuku must be running to *change* the saturation. Once applied, the filter stays until the next reboot or change, even if Shizuku stops.
- The saturation resets on every reboot. GrayDial deliberately does not reapply it silently; it shows a Reapply prompt instead.
- Android exposes only whether the filter is on or off, not its exact level. GrayDial cannot independently verify "75%".
- Changes go through Android's short built-in fade animation.
- Other features that use the same system control (Digital Wellbeing Bedtime grayscale, some vendor modes) override GrayDial, and vice versa. The last change wins.
- Interaction with HDR, secure video and HyperOS display modes is untested.
- The name "GrayDial" and the package `com.graydial.android` are provisional.

## Documentation

- [ARCHITECTURE.md](ARCHITECTURE.md): how the app is structured.
- [SECURITY.md](SECURITY.md): security model and review findings.
- [PRIVACY.md](PRIVACY.md): what data the app stores.
- [TECHNICAL_FEASIBILITY.md](TECHNICAL_FEASIBILITY.md): Phase 0 research.
- [TESTING.md](TESTING.md): test results and the device checklist.
- [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md): third-party licenses.
- [DEPENDENCIES.md](DEPENDENCIES.md): dependency inventory.

## License

No license has been granted yet. All rights reserved by the project owner.
