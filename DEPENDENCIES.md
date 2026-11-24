# Dependency inventory

Generated from `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` on 2026-11-24.
These are the libraries packaged into the release APK. Test-only dependencies (JUnit 4.13.2 — EPL-1.0, kotlinx-coroutines-test 1.11.0 — Apache-2.0, AndroidX Test ext-junit 1.3.0 / runner 1.7.0 — Apache-2.0) are not shipped.

All artifacts are pinned and their SHA-256 checksums are recorded in `gradle/verification-metadata.xml` (Gradle dependency verification is active). All transitive versions are locked in `app/gradle.lockfile`.

Total runtime artifacts: 123

| Artifact | Version | License | Source | Direct |
|---|---|---|---|---|
| `androidx.activity:activity` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.activity:activity-compose` | 1.13.0 | Apache-2.0 | Google Maven | yes |
| `androidx.activity:activity-ktx` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.annotation:annotation` | 1.10.0 | Apache-2.0 | Google Maven |  |
| `androidx.annotation:annotation-experimental` | 1.4.1 | Apache-2.0 | Google Maven |  |
| `androidx.annotation:annotation-jvm` | 1.10.0 | Apache-2.0 | Google Maven |  |
| `androidx.arch.core:core-common` | 2.2.0 | Apache-2.0 | Google Maven |  |
| `androidx.arch.core:core-runtime` | 2.2.0 | Apache-2.0 | Google Maven |  |
| `androidx.autofill:autofill` | 1.0.0 | Apache-2.0 | Google Maven |  |
| `androidx.collection:collection` | 1.6.0 | Apache-2.0 | Google Maven |  |
| `androidx.collection:collection-jvm` | 1.6.0 | Apache-2.0 | Google Maven |  |
| `androidx.collection:collection-ktx` | 1.6.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose:compose-bom` | 2026.11.01 | Apache-2.0 | Google Maven | yes |
| `androidx.compose.animation:animation` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.animation:animation-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.animation:animation-core` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.animation:animation-core-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.foundation:foundation` | 1.13.0 | Apache-2.0 | Google Maven | yes |
| `androidx.compose.foundation:foundation-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.foundation:foundation-layout` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.foundation:foundation-layout-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.material:material-ripple` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.material:material-ripple-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.material3:material3` | 1.5.0 | Apache-2.0 | Google Maven | yes |
| `androidx.compose.material3:material3-android` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.material3:material3-ripple` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.material3:material3-ripple-android` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.runtime:runtime` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.runtime:runtime-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.runtime:runtime-annotation` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.runtime:runtime-annotation-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.runtime:runtime-retain` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.runtime:runtime-retain-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.runtime:runtime-saveable` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.runtime:runtime-saveable-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui` | 1.13.0 | Apache-2.0 | Google Maven | yes |
| `androidx.compose.ui:ui-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui-geometry` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui-geometry-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui-graphics` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui-graphics-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui-text` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui-text-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui-unit` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui-unit-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui-util` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.compose.ui:ui-util-android` | 1.13.0 | Apache-2.0 | Google Maven |  |
| `androidx.concurrent:concurrent-futures` | 1.1.0 | Apache-2.0 | Google Maven |  |
| `androidx.core:core` | 1.19.1 | Apache-2.0 | Google Maven |  |
| `androidx.core:core-ktx` | 1.19.1 | Apache-2.0 | Google Maven | yes |
| `androidx.core:core-viewtree` | 1.0.0 | Apache-2.0 | Google Maven |  |
| `androidx.customview:customview-poolingcontainer` | 1.0.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore-android` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore-core` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore-core-android` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore-core-okio` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore-core-okio-jvm` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore-preferences` | 1.3.0 | Apache-2.0 | Google Maven | yes |
| `androidx.datastore:datastore-preferences-android` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore-preferences-core` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore-preferences-core-android` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore-preferences-external-protobuf` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.datastore:datastore-preferences-proto` | 1.3.0 | Apache-2.0 | Google Maven |  |
| `androidx.emoji2:emoji2` | 1.7.0 | Apache-2.0 | Google Maven |  |
| `androidx.graphics:graphics-path` | 1.1.0 | Apache-2.0 | Google Maven |  |
| `androidx.graphics:graphics-shapes` | 1.0.1 | Apache-2.0 | Google Maven |  |
| `androidx.graphics:graphics-shapes-android` | 1.0.1 | Apache-2.0 | Google Maven |  |
| `androidx.interpolator:interpolator` | 1.0.0 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-common` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-common-java8` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-common-jvm` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-livedata-core` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-process` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-runtime` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-runtime-android` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.11.1 | Apache-2.0 | Google Maven | yes |
| `androidx.lifecycle:lifecycle-runtime-compose-android` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-runtime-ktx-android` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-viewmodel` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-viewmodel-android` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.11.1 | Apache-2.0 | Google Maven | yes |
| `androidx.lifecycle:lifecycle-viewmodel-compose-android` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-viewmodel-savedstate` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.lifecycle:lifecycle-viewmodel-savedstate-android` | 2.11.1 | Apache-2.0 | Google Maven |  |
| `androidx.navigationevent:navigationevent` | 1.1.0 | Apache-2.0 | Google Maven |  |
| `androidx.navigationevent:navigationevent-android` | 1.1.0 | Apache-2.0 | Google Maven |  |
| `androidx.navigationevent:navigationevent-compose` | 1.1.0 | Apache-2.0 | Google Maven |  |
| `androidx.navigationevent:navigationevent-compose-android` | 1.1.0 | Apache-2.0 | Google Maven |  |
| `androidx.profileinstaller:profileinstaller` | 1.4.0 | Apache-2.0 | Google Maven |  |
| `androidx.savedstate:savedstate` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `androidx.savedstate:savedstate-android` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `androidx.savedstate:savedstate-compose` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `androidx.savedstate:savedstate-compose-android` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `androidx.savedstate:savedstate-ktx` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `androidx.startup:startup-runtime` | 1.1.1 | Apache-2.0 | Google Maven |  |
| `androidx.tracing:tracing` | 2.0.1 | Apache-2.0 | Google Maven |  |
| `androidx.tracing:tracing-android` | 2.0.1 | Apache-2.0 | Google Maven |  |
| `androidx.versionedparcelable:versionedparcelable` | 1.1.1 | Apache-2.0 | Google Maven |  |
| `androidx.window:window` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `androidx.window:window-core` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `androidx.window:window-core-android` | 1.5.0 | Apache-2.0 | Google Maven |  |
| `com.google.guava:listenablefuture` | 1.0 | Apache-2.0 | Maven Central |  |
| `com.squareup.okio:okio` | 3.17.0 | Apache-2.0 | Maven Central |  |
| `com.squareup.okio:okio-jvm` | 3.17.0 | Apache-2.0 | Maven Central |  |
| `dev.rikka.shizuku:aidl` | 13.1.5 | MIT | Maven Central (RikkaApps/Shizuku-API) |  |
| `dev.rikka.shizuku:api` | 13.1.5 | MIT | Maven Central (RikkaApps/Shizuku-API) | yes |
| `dev.rikka.shizuku:provider` | 13.1.5 | MIT | Maven Central (RikkaApps/Shizuku-API) | yes |
| `dev.rikka.shizuku:shared` | 13.1.5 | MIT | Maven Central (RikkaApps/Shizuku-API) |  |
| `org.jetbrains:annotations` | 23.0.0 | Apache-2.0 | Maven Central |  |
| `org.jetbrains.kotlin:kotlin-stdlib` | 2.4.21 | Apache-2.0 | Maven Central | yes |
| `org.jetbrains.kotlinx:atomicfu` | 0.28.0 | Apache-2.0 | Maven Central |  |
| `org.jetbrains.kotlinx:atomicfu-jvm` | 0.28.0 | Apache-2.0 | Maven Central |  |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.11.0 | Apache-2.0 | Maven Central | yes |
| `org.jetbrains.kotlinx:kotlinx-coroutines-bom` | 1.11.0 | Apache-2.0 | Maven Central |  |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core` | 1.11.0 | Apache-2.0 | Maven Central |  |
| `org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm` | 1.11.0 | Apache-2.0 | Maven Central |  |
| `org.jetbrains.kotlinx:kotlinx-serialization-bom` | 1.7.3 | Apache-2.0 | Maven Central |  |
| `org.jetbrains.kotlinx:kotlinx-serialization-core` | 1.7.3 | Apache-2.0 | Maven Central |  |
| `org.jetbrains.kotlinx:kotlinx-serialization-core-jvm` | 1.7.3 | Apache-2.0 | Maven Central |  |
| `org.jspecify:jspecify` | 1.0.0 | Apache-2.0 | Maven Central |  |

## License summary

- Apache-2.0: all AndroidX, Kotlin, kotlinx, Okio, JSpecify, JetBrains annotations, Guava listenablefuture.
- MIT: Shizuku API (`dev.rikka.shizuku:*`).
- No GPL/LGPL/AGPL or other copyleft licenses are packaged. Both licenses permit proprietary/commercial distribution with attribution (see THIRD_PARTY_NOTICES.md).

## Known-vulnerability scan

No vulnerability scanner (e.g. OWASP Dependency-Check, OSV-Scanner) was available in the build environment, so no automated CVE scan was run. Recommended before release:

```bash
osv-scanner scan source --lockfile app/gradle.lockfile
```

Regenerate this file whenever dependencies change.
