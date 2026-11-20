# Telemetry and Logging

## Capturing an intermittent app hang during practice

Connect the laptop to the robot over ADB, then run from the repository root:

```powershell
.\scripts\capture-hang.cmd
```

On Linux, use `bash scripts/capture-hang.sh`. Python 3 and Android platform-tools
are required. With multiple ADB devices, add `--serial <device-serial>`.
This does not deploy or modify robot code. Keep the terminal open; Ctrl+C stops it.

The command saves continuous Logcat to a separate timestamped folder under the
computer's temporary `robot-captures` directory and prints that folder's path.
Use `--output <directory>` to choose a longer-lived location. Logs can contain
network credentials and other private details; review them before sharing.

After receiving a live Sinister app heartbeat, it prints **ARMED**. A heartbeat
gap longer than 6.5 seconds triggers an ADB SIGQUIT request for a Java thread dump,
before the observed ten-second Control Hub watchdog deadline. It copies Android's
thread trace and the retained robot log into a numbered capture folder, then
rearms after heartbeats resume. It runs until stopped, with no match time limit.

This watches the Sinister heartbeat used by this project, not the OpMode loop.
A heartbeat gap can also reflect a deployment or connection problem. Dump requests
may fail or finish too late if Android or ADB is unresponsive; inspect `status.txt`,
`signal.txt`, and the thread files before assuming a dump was captured. A thread
dump briefly adds diagnostic work, so use this for practice diagnosis. If ADB's
log stream ends, reconnect and restart the command. No logs are automatically
sent anywhere. Long sessions can consume disk space; archive or delete captures
after investigation.

Practice workflow:

1. Start the command and wait for **ARMED** before reproducing the problem.
2. Note the approximate failure time and driver action. Let the watcher collect
   its files; restarting or clearing logs immediately can lose evidence.
3. After practice, press Ctrl+C and open the printed session folder.
4. Read `status.txt` to locate the capture, then check its `hang-N` folder.
   `signal.txt` records whether the dump request failed. `threads-0.txt` through
   `threads-2.txt` are successive attempts to read Android's thread dump; verify
   their process and time match the incident, since Android may retain an older
   dump or return a permission error. `robotControllerLog.txt` contains the
   retained robot log; the session's `logcat.txt` provides continuous context.
5. Preserve the complete session folder with your notes for diagnosis. A saved
   folder or successful signal alone does not establish the cause of the freeze.

Telemetry and logging share level names and optional filters, but they solve
different problems and have different lifetimes.

## Telemetry (`tel`)

Telemetry is current state. A subsystem owns a source-scoped `Tel` instance:

```kotlin
tel.info("Artifacts", artifacts)
tel.debug("Laser", blocked)
```

Visible entries are rebuilt every frame and sent through a joined output to the FTC
Driver Station and Panels. A line is shown only when:

1. `Telemetry.LEVEL` accepts its level;
2. The shared display filter matches the combined level/source/caption/value text;
3. The telemetry-specific filter also matches.

Lazy value overloads avoid computing expensive diagnostic values when the level is
not accepted. Stable numeric display should use an explicit format string so the
number of decimal places does not jump between frames.

## Logging (`log`)

Logging records events and transitions:

```kotlin
log.warn("Target unavailable")
```

Every non-off entry goes immediately to FTC `RobotLog` at the matching Android
priority, regardless of whether it is visible on Driver Station. The entry is also
retained in a bounded in-memory history. Accepted history entries are rendered as
ordinary telemetry lines below the `LOG` divider on each frame. They are not sent
through SDK telemetry.log().add() or clear(). The SDK log capacity getter still
sets the retained history bound; in RobotCore 12.0.0 that getter takes no lock.

Command snapshots are logged only when the active named command set changes. This
provides scheduler evidence without logging the same state every periodic loop.

## Levels

Both systems intentionally define their own enum even though the names match:

| Level | Indicator | Intended use |
|---|---:|---|
| `VERBOSE` | V | High-frequency or hardware-detail diagnostics |
| `DEBUG` | D | Development state and scheduler detail |
| `INFO` | I | Useful default operator state or meaningful event |
| `WARN` | W | Degraded but continuing behavior |
| `ERROR` | E | Failed behavior or disabled subsystem |
| `ASSERT` | A | Fatal/invariant failure, exposed as `fatal()` |
| `OFF` | — | Disable the destination/filter pipeline |

Telemetry and log levels remain independent in Panels. Decode config provides one
convenient `level` default for both because drivers commonly change them together.
The default is `INFO`: routine subsystem state may be visible while detailed
hardware state remains debug/verbose. The reusable base is capable of defaulting
off if no compatible config exists.

## Display Sections

The Driver Station output is deliberately structured:

```text
------------------------- CONFIG -------------------------
Alliance: RED
Side: NORTH
--------------------------- TEL ---------------------------
I | Drive | Power: 1.00
I | Intake | Artifacts: 2
--------------------------- LOG ---------------------------
D Commands | Running | Drive.toDeposit
```

`CONFIG` is always generated by the config subsystem. `TEL` appears when at least
one telemetry entry passes filtering. `LOG` appears only when the retained log has
visible entries. Clearing the current frame and rebuilding the log are necessary to
remove stale Driver Station lines when a level or filter becomes more restrictive.

## Filtering

The shared config `filter` is a case-insensitive substring applied to the complete
renderable line. This lets `def`, `Gate`, or `position` match source, caption,
message, or value. Panels also exposes destination-specific filters for cases where
live telemetry and historical logs need different focus.

## Choosing an API

- Use `tel` for a value whose latest state matters and can replace its last value.
- Use `log` for something that happened and may matter after the state changes.
- Do not duplicate every telemetry value into Logcat. Log transitions, failures,
  decisions, and notable actions instead.
- Put team-specific hardware field selection in TeamCode's `tel()` extensions.

## Loop breadcrumbs and hang monitor

The breadcrumb/hang-monitor experiment is archived as
[docs/assets/diagnostics/loop-breadcrumbs.patch](../assets/diagnostics/loop-breadcrumbs.patch).
It is not compiled or enabled in normal OpModes. The telemetry lock-order fix is
independent and remains active. The patch restores the library classes, callback
instrumentation, OpMode wiring and their tests together.

To restore it temporarily from the repository root:

```sh
git apply --check docs/assets/diagnostics/loop-breadcrumbs.patch
git apply docs/assets/diagnostics/loop-breadcrumbs.patch
```

If the check fails after later code changes, adapt the patch to the current
lifecycle; do not force it or overwrite unrelated work. Run the affected
HangMonitorTests, SubsystemComponentTests and OpModeTests, then Deploy Full because
this adds reusable classes. Keep the diagnostic changes local while investigating.

The restored monitor records nested component callbacks, individual subsystem
periodic updates and default scheduling boundaries without logging each boundary.
Its independent daemon samples every 250 ms and emits one direct RobotLog ERROR
when a breadcrumb has not advanced for two seconds. Filter Logcat for HangMonitor:

```text
E HangMonitor: No loop progress for 2100 ms | Turret.periodic
```

It bypasses telemetry and normal log history, rearms after progress, closes on
normal or exceptional OpMode exit, and never changes hardware outputs or command
policy. It does not collect Java stacks. A whole-runtime freeze may stop the
monitor too. CommandManager / loop-transition labels include framework work
between callbacks and do not prove a particular command is blocked.

Save RobotControllerLog.txt and its rotated files after a freeze. Use the last
phase to add narrower boundaries for the next reproduction. Existing capture-hang
scripts remain useful for collecting surrounding logs. Once finished, first check
and then reverse the patch:

```sh
git apply --reverse --check docs/assets/diagnostics/loop-breadcrumbs.patch
git apply --reverse docs/assets/diagnostics/loop-breadcrumbs.patch
```

If diagnostic code has changed since applying it, remove those changes manually
rather than using a destructive reset. Deploy Full again to remove the diagnostic
classes from the installed robot app.

## SDK telemetry-log lock ordering

REQ-PLT-025 preserves RobotLog events and the Driver Station LOG presentation while
avoiding SDK log mutations. RobotCore 12.0.0 TelemetryImpl has two distinct monitors:
its theLock field (A) and the TelemetryImpl instance used by LogImpl.getLock() (B).
The SDK update path holds A then requests B while composing log entries. Conversely,
LogImpl.add() holds B and calls tryUpdate(), which requests A. Concurrent SDK
EventLoop updates and OpMode log additions can therefore request opposite locks.

TelemetryLockOrderTests exercises the actual SDK class with latches and probes at
its protected/package boundaries. Once both paths are reached, the test intercepts
the updater's B request and throws a test-only exception to release A. This proves
the reachable inversion without leaving permanently deadlocked test threads. It
does not prove that the earlier on-robot hangs were caused by this inversion; brief
lock-contention samples and the debugger's separate ART assertion are insufficient.

Logging.add now writes RobotLog and retains history only. Telemetry.update draws
filtered retained lines after LOG and before the normal joined update. No SDK log
add/clear calls occur in that integration. Filtering is reevaluated every frame;
normal telemetry clear removes stale lines. Existing levels, history capacity and
command snapshot events remain. TelemetryComponentTests checks presentation,
filters and capacity and rejects any SDK log add/clear invocation; LoggingTests
checks RobotLog routing and retention. Consumers can keep their existing
diagnostics unchanged while testing this fix.
