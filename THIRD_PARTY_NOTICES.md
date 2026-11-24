# Third-party notices

GrayDial's own source code, icon, and UI design are original works by the project owner.

**License status:** GrayDial's own code is **not** licensed under any open-source license. All rights are reserved by the owner until the owner chooses a license.

GrayDial includes the following third-party components. Each entry gives the component, its provenance (where it came from), and its license.

## Shizuku API: MIT License

- **Artifacts:** `dev.rikka.shizuku:api`, `provider`, `shared` and `aidl`, version 13.1.5.
- **Source:** https://github.com/RikkaApps/Shizuku-API
- **Copyright:** Copyright (c) 2021 RikkaW

```
MIT License

Copyright (c) 2021 RikkaW

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## Apache License 2.0 components

- **Copyright holders:** The Android Open Source Project / Google LLC, JetBrains s.r.o., Square Inc., the JSpecify authors, and the Guava authors.
- **License text:** https://www.apache.org/licenses/LICENSE-2.0
- **Components:**
  - AndroidX: Compose UI/Foundation/Material3/Runtime/Animation, Core, Activity, Lifecycle, DataStore, SavedState, Startup, ProfileInstaller, Emoji2, Collection, Annotation, Tracing, Window, Graphics and related transitive modules.
  - Kotlin standard library and kotlinx-coroutines.
  - Okio (transitive, via DataStore).
  - JSpecify and JetBrains annotations.
  - Guava `listenablefuture`.

The full list with versions is in `DEPENDENCIES.md`. None of these components has a NOTICE file that requires attribution beyond the license.

## Reference material (not copied)

The saturation matrix coefficients (0.231, 0.715, 0.072) are documented from AOSP `GlobalSaturationTintController` (Apache 2.0). GrayDial reimplements the formula independently for its preview; no AOSP source code is included.

## Distribution obligations

- **MIT and Apache 2.0:** both permit commercial and proprietary distribution. They require you to keep the copyright and license notices; this file satisfies that requirement when shipped with the app or its listing.
- **Apache 2.0, additionally:** modified Apache files must carry a notice of the change. GrayDial does not modify any of them.
- **Copyleft:** no copyleft (GPL/LGPL/AGPL/MPL) code is packaged.
- **Test-only:** JUnit 4 (EPL-1.0) is used only for tests and is not distributed.

## Trademarks

"GrayDial" is a provisional name, pending trademark clearance. "Android" is a trademark of Google LLC. "Shizuku" is the name of RikkaApps' project. "Xiaomi" and "HyperOS" are trademarks of Xiaomi.
