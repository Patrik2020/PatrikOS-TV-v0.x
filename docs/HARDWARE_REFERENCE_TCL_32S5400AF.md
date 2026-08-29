# Reference device: TCL 32S5400AF

The first PatrikOS TV development target is the physical television inspected before this repository was initialized.

## Confirmed from labels / board photographs

- Model: TCL `32S5400AF`
- Variant: `32S5400AFX1`
- Series marking: S5
- Mainboard/chassis marking: `TPD.MT9221T.PB779(T)`
- Board family marking: `40-MT21XA-MPC2HG-C`
- MT21 / MT9221 platform markings are present on the board
- Panel label: `LVF320CSDX E0012`
- Wi-Fi module: `WBER2500`, TCL part `30130-000012`
- Firmware-family label observed: `V8-T221T01-LF1V121`
- The board integrates mains power circuitry and has a clearly marked `HOT` / `COLD` isolation boundary.

## Expected but still to verify on the actual unit with ADB

Public specifications for this model/platform indicate Android TV 11 and a low-memory configuration. PatrikOS will not treat RAM, storage, CPU/GPU, security patch level or partition data as confirmed until the running unit reports them.

Planned capture:

```bash
adb shell getprop ro.build.version.release
adb shell getprop ro.build.version.sdk
adb shell getprop ro.build.fingerprint
adb shell getprop ro.product.manufacturer
adb shell getprop ro.product.model
adb shell cat /proc/meminfo
adb shell df -h
adb shell dumpsys meminfo
```

## Safety note

The open mainboard contains a mains-connected `HOT` section. Software development and ADB profiling do not require the TV to be operated while exposed. Hardware probing is outside the launcher track and belongs only in the separate experimental research repository with an explicit recovery and electrical-safety plan.
