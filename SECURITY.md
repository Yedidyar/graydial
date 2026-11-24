# Security

## Threat model in one paragraph

GrayDial needs one privileged capability: setting global display saturation. Shizuku provides shell-UID (2000) privileges. These are **much broader** than that one capability: Shizuku's shell can install packages, grant permissions, read logs, inject input and more. GrayDial cannot narrow what Shizuku itself can do. GrayDial's design goal is to make sure **GrayDial's own code never exposes more than "set saturation 0–100"**, so that GrayDial cannot be abused as a stepping stone, even by a malicious app on the same device.

> Shizuku may possess broader system privileges than those exposed by GrayDial. Only authorize apps you trust in Shizuku. You can revoke GrayDial at any time in Shizuku › Authorized applications.

## Privileged boundary

All privileged code is in `system/ShizukuColorDisplay.kt`. It wraps the system `color_display` binder in `ShizukuBinderWrapper`, so each call reaches ColorDisplayService as the shell UID. GrayDial starts **no** privileged process of its own and has no IPC interface of its own.

It performs exactly two fixed operations:

| Operation | Mechanism |
|---|---|
| Set saturation | Binder shell-command transaction (`'_CMD'`, the entry point behind `adb shell cmd`). Arguments: the constant `set-saturation` and `Int.toString()` of a value checked to be in 0..100 before any call. |
| Read state | Binder dump transaction on the same service. GrayDial parses only the `Global saturation: Activated: true/false` line, reading at most 2,000 lines for at most 2 s. |

How the boundary is enforced:

- **Validation twice.** GrayDial rejects values outside 0..100 before calling. ColorDisplayService validates the value again and returns −1 if it is out of range (verified on the device with 150).
- **No shell, no arbitrary commands.** No process is spawned and there is no `sh -c`. The argument array is built from a constant and an integer, never from external strings.
- **No inputs from other apps.** Nothing exported accepts parameters that reach this code.
- **Output discarded.** Command output goes to `/dev/null`. Success is the result code delivered through a `ResultReceiver`.
- **Timeouts.** The result is awaited for 5 s and the whole operation is capped at 7 s. A missing result is reported as a failure, never as success.

Residual note: Shizuku may keep its copies of the file descriptors passed in these calls. They point to `/dev/null` or to a pipe GrayDial closes, so nothing sensitive is exposed.

## Android components (merged release manifest, inspected 2026-11-24)

| Component | Exported | Protection |
|---|---|---|
| `MainActivity` | yes (launcher, `QS_TILE_PREFERENCES`) | Reads no intent data or extras. |
| `GrayDialTileService` | yes | `android.permission.BIND_QUICK_SETTINGS_TILE` (system only). |
| `rikka.shizuku.ShizukuProvider` | yes | `android.permission.INTERACT_ACROSS_USERS_FULL` (signature/privileged; only system/shell such as the Shizuku server can call it). Required for Shizuku to deliver its binder. |
| `androidx.startup.InitializationProvider` | no | n/a |
| `androidx.profileinstaller.ProfileInstallReceiver` | yes | `android.permission.DUMP` (shell/system only). Comes from Compose; installs baseline profiles for faster startup. |

## Permissions in the merged manifest

| Permission | Source | Why |
|---|---|---|
| `moe.shizuku.manager.permission.API_V23` | Shizuku provider library | Lets the Shizuku manager identify GrayDial as a Shizuku client. |
| `com.graydial.android.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | androidx.core | Signature-level self-permission AndroidX uses to protect non-exported dynamic receivers. |

**Absent:** `INTERNET`, `ACCESS_NETWORK_STATE`, location, camera, microphone, contacts, SMS, storage, `QUERY_ALL_PACKAGES`, `FOREGROUND_SERVICE`, `RECEIVE_BOOT_COMPLETED`, `SYSTEM_ALERT_WINDOW`, accessibility, MediaProjection.

Package visibility is limited to `<queries><package android:name="moe.shizuku.privileged.api"/></queries>`.

Check it yourself:

```bash
./gradlew :app:processReleaseMainManifest && grep -E "uses-permission|exported=\"true\"" app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml
```

## Network isolation

- There is no `INTERNET` permission. The OS denies sockets to the app.
- There are no analytics, crash reporting, ads, remote config, or dynamic code loading.
- The "Get Shizuku" button opens the Play Store, or failing that a browser, through an `ACTION_VIEW` intent. The network request is made by that other app, not by GrayDial.

## Data

- DataStore holds four values: enabled, intensity, last applied saturation, and boot count/timestamp.
- `allowBackup=false`, and the data-extraction rules exclude everything from cloud backup and device transfer.

## Supply chain

- Every version is pinned in `gradle/libs.versions.toml`, and all transitive versions are locked in `app/gradle.lockfile`.
- **Gradle dependency verification** is enabled: `gradle/verification-metadata.xml` holds SHA-256 checksums for 370 components, plugins included.
- The full runtime inventory and licenses are in `DEPENDENCIES.md`.
- No secrets or signing material are in the repository. `.gitignore` excludes `*.jks`, `*.keystore`, `keystore.properties` and `local.properties`. The release build is unsigned; sign it out-of-tree.

## Phase 4 review findings and resolutions

| # | Finding | Resolution |
|---|---|---|
| 1 | A plan to remove `ProfileInstallReceiver` used `tools:node="remove"`, but lint showed the class is not on the debug classpath, so the directive was wrong there. | Removed the directive and kept the receiver. It is DUMP-protected and documented above. |
| 2 | A hung binder call could block the caller indefinitely. | Calls run outside structured concurrency with a 7 s timeout and are reported as `Timeout`. |
| 3 | The Shizuku user service failed to start on Android 16 (crash inside Shizuku's starter), and it added a separate privileged process. | The user service and its AIDL interface were removed. The app now makes single binder calls through Shizuku (above). |
| 3a | Reading command output to end-of-stream hung, because Shizuku kept its copy of the pipe open. | Command output now goes to `/dev/null`; dump reads have a deadline. |
| 4 | The tile could report success optimistically. | The tile renders only persisted, successfully applied state. |
| 5 | Stale state after reboot could claim grayscale is active. | Added a boot-count check that marks the state stale and switches the master toggle off. |
| 6 | Preferences could leak through backups. | Set `allowBackup=false` and added exclusion rules. |
| 7 | Backups or tampering could insert out-of-range stored values. | They are ignored on read and replaced with defaults. |

## Residual risks

- Shizuku's own privilege scope, explained above.
- The shell-command and dump transaction codes are stable framework constants, but `set-saturation` and the dump format belong to ColorDisplayService and could change in a future Android release. If they do, GrayDial reports a failure or "could not be read back". It never reports false success.
- No automated CVE scan was run because no scanner was available offline. See DEPENDENCIES.md.

## Reporting

Report security issues privately to the project owner. Do not open public issues for vulnerabilities.
