# Deploy Robot Code

Quickstart provides two deliberate deployment choices. Their names are shared by
Android Studio run configurations and the Windows/Linux scripts in the repository
root.

| Workflow | Use it for | What it does |
|---|---|---|
| **Deploy Sloth** | Fast iteration on ordinary `TeamCode` Kotlin/Java behavior | Builds and pushes a TeamCode overlay without reinstalling the full Robot Controller APK. |
| **Deploy Full** | First install, release validation, and changes outside Sloth's safe scope | Removes any remote Sloth overlay, builds the debug APK, and installs the complete application. |

Before transferring code, both workflows verify the configured Robot Controller
with a short ADB shell round trip. A healthy connection continues immediately. If
the check fails or times out, deployment clears only that controller's stale TCP
transport, reconnects it, and verifies another round trip. This handles the stale
connection that can remain after a controller is powered off without an explicit
ADB disconnect. If that targeted recovery also fails, the preflight restarts the
local ADB server once and repeats the disconnect, connect, and probe sequence. This
last resort mirrors the manual recovery sometimes needed after the computer changes
Wi-Fi networks.

The standard Control Hub endpoint is committed in `gradle.properties`:

```properties
robotControllerAddress=192.168.43.1:5555
```

Change that value if the team's Robot Controller uses another address. The normal
preflight targets only this controller. The last-resort server restart briefly
resets every ADB connection on the computer, but it runs only after the targeted
recovery has already failed.

## First Installation

Run **Deploy Full** before using Sloth on a controller. This installs the Robot
Controller application, the Sloth runtime, and the compatible Panels runtime.

- Android Studio: choose the shared **Deploy Full** Gradle run configuration.
- Windows: run `deploy-full.cmd`.
- Linux: run `./deploy-full.sh`.

The underlying Gradle task is `:TeamCode:installDebug`. Sloth's Load plugin makes
that install depend on `removeSlothRemote`, which deletes only:

```text
/storage/emulated/0/FIRST/dairy/sloth/loaded.jar
```

That automatic cleanup matters: otherwise an older overlay could continue taking
precedence over classes in the newly installed APK.

## Fast Iteration

After the full install, use **Deploy Sloth** for normal edits within `TeamCode`.

- Android Studio: choose the shared **Deploy Sloth** Gradle run configuration.
- Windows: run `deploy-sloth.cmd`.
- Linux: run `./deploy-sloth.sh`.

All three shared entry points run `:TeamCode:deploySloth` with Gradle's `--offline`
option. This prevents dependency repository checks from delaying field-side iteration.
It does not disable ADB communication with the Robot Controller. If a required
artifact is absent from the local Gradle cache, deployment fails promptly and that
computer must perform a full online build before Sloth can be used.

End the active OpMode before judging
the change; Sloth applies loaded code at the OpMode boundary. The overlay persists
across Robot Controller restarts and controller power cycles until replaced or
removed by a full deployment.

Use **Deploy Full** instead when changing:

- Gradle dependencies or plugins;
- `3drdNextFTC`, `FtcRobotController`, manifests, or Android resources;
- application packaging or startup integration;
- classes that were added, removed, renamed, or structurally reshaped, until that
  case has been validated on the team's controller;
- code marked `@Pinned` by Sloth.

## Competition Use

The Sloth overlay can run robot code, but it should not be the only state tested for
a competition build. Use **Deploy Full** before release/competition validation and
run the robot's smoke tests from that clean APK. This proves the complete package
and eliminates uncertainty about a persistent overlay.

If Sloth is used for an urgent field-side TeamCode correction, test the affected
behavior immediately and follow with a full deployment as soon as practical.

## Compatibility Checklist

Before treating Sloth as established team workflow, verify on a physical controller:

1. **Deploy Full** installs and launches Teleop with Panels connected.
2. A harmless visible TeamCode change appears after **Deploy Sloth** and a fresh
   OpMode initialization.
3. Repeated stop/init cycles do not duplicate controls or retain old subsystem
   instances.
4. **Deploy Full** after a Sloth change restores the APK implementation, proving the
   remote overlay was removed.
5. Robot behavior, telemetry, logging, and field drawing still work from the clean
   full deployment.
6. After powering off the controller without manually disconnecting ADB, either
   deployment reconnects automatically once the controller and its Wi-Fi are back.
7. After an online full build has primed the computer, **Deploy Sloth** succeeds
   after restarting Android Studio with internet access unavailable.
