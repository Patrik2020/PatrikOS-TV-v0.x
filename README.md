# PatrikOS TV

**PatrikOS TV** is the lightweight Android TV / Google TV launcher track of the PatrikOS project. Its first reference device is a TCL 32S5400AF that feels slow under the stock Android TV interface.

The goal is deliberately narrower than a custom ROM: make weak TV hardware pleasant to use without replacing firmware or risking the television.

## v0.1 prototype

The first prototype provides:

- a minimal, D-pad-first home screen;
- automatic discovery of installed Android TV apps;
- direct app launching;
- system Settings shortcut;
- clock and basic device/RAM summary;
- `HOME` intent support so compatible firmware can select PatrikOS as the launcher;
- no internet permission, analytics, ads or telemetry;
- no `QUERY_ALL_PACKAGES` permission: discovery is scoped to TV launcher activities.

The UI uses classic Android Views rather than Compose to keep the dependency and runtime footprint small on low-RAM televisions.

## Target / compatibility

- `minSdk 21` (Android 5.0)
- `targetSdk 34` (Android 14)
- primary test target: TCL 32S5400AF / Android TV 11
- remote control / D-pad is the primary input method

Android TV has a separate Google Play target-API exception; API 34 remains acceptable for new TV apps at the 31 August 2026 policy change. We still test behavior on newer versions before a store release.

## First real-device boot

PatrikOS TV v0.1 successfully installed over ADB and launched on the physical TCL 32S5400AF reference television on 2026-08-29. The launcher discovered the installed TV applications, rendered the D-pad UI, displayed the live clock, and correctly reported the Android 11 / API 30 environment and approximately 0.9 GB of RAM.

![PatrikOS TV v0.1 first boot on TCL 32S5400AF](docs/assets/patrikos-tv-v0.1-first-boot.jpg)

The image is cropped to the television screen so the public repository does not expose unrelated personal surroundings.

## Build

Open the repository in Android Studio with JDK 17, or use Gradle 8.9:

```bash
gradle :app:assembleDebug
```

The debug APK is produced at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

CI also builds the APK on every feature-branch push and pull request.

## Install on a TV

After Android developer options and network/USB debugging are enabled:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

PatrikOS can always be opened as a normal TV app. On firmware that permits changing the HOME activity, ADB can also request it as the home launcher:

```bash
adb shell cmd package set-home-activity hu.patrikos.tv/.MainActivity
```

OEM firmware can restrict third-party HOME replacement, so we test this on each device family instead of assuming it works everywhere.

## Project rules

1. The launcher track never flashes firmware or modifies boot/recovery/system partitions.
2. Performance claims require measurements on real hardware.
3. Privacy is a feature: installed-app data stays on the TV and is not sent anywhere.
4. Manufacturer-specific integrations must have a generic fallback.
5. Keep dependencies minimal until a feature justifies the cost.

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) and [`docs/HARDWARE_REFERENCE_TCL_32S5400AF.md`](docs/HARDWARE_REFERENCE_TCL_32S5400AF.md).

## Licensing

No open-source license has been granted yet. Publication of this repository does not grant permission to copy, redistribute, rebrand or commercially exploit the code. Licensing will be decided before a public product release.
