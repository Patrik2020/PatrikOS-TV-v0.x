# PatrikOS TV architecture

## Scope

This repository is the safe launcher track of PatrikOS. It does not replace TCL firmware, modify partitions, require root, or touch the bootloader.

## Design goals

- Fast startup on low-memory Android TV hardware.
- D-pad-first navigation with no touch dependency.
- No network permission, analytics, advertising SDK, or telemetry in the launcher core.
- No `QUERY_ALL_PACKAGES`; app discovery is scoped to activities that advertise `LEANBACK_LAUNCHER`.
- Minimal dependency graph: platform APIs plus RecyclerView.
- Firmware-independent code where Android APIs allow it.

## Components

`MainActivity`
: HOME/TV entry point, focus management, system settings shortcut and device summary.

`AppRepository`
: performs package discovery off the UI thread and returns launchable TV apps.

`AppAdapter`
: lightweight RecyclerView binding for remote-control navigation.

## Performance budget for the TCL reference device

These are targets, not measured claims yet:

- cold launcher start: <= 2 s
- idle proportional set size (PSS): <= 80 MB
- zero network requests from launcher core
- no package enumeration on the main thread
- smooth D-pad movement without continuous animations

## Benchmark commands

After ADB is enabled on the TV:

```bash
adb shell am force-stop hu.patrikos.tv
adb shell am start -W -n hu.patrikos.tv/.MainActivity
adb shell dumpsys meminfo hu.patrikos.tv
```

Record measurements before and after each major optimization. Marketing claims must use reproducible measurements rather than estimates.

## Next milestones

1. v0.1: app grid, HOME intent, system settings, CI build.
2. v0.2: favorites and stable ordering stored locally.
3. v0.3: input/source integration via capability adapters where manufacturers expose safe APIs.
4. v0.4: optional child profile and launcher configuration.
5. v0.5: Play-ready artwork, accessibility review, TV quality checklist and device matrix.
