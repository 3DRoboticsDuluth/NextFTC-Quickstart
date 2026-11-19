"""Save practice logs and request a thread dump when Sinister heartbeats stall."""
import argparse
import datetime
import os
from pathlib import Path
import queue
import re
import shutil
import subprocess
import tempfile
import threading
import time


class Heartbeat:
    def __init__(self, timeout=6.5):
        self.timeout = timeout
        self.last = None

    def observe(self, now):
        self.last = now

    def stalled(self, now):
        if self.last is None or now - self.last <= self.timeout:
            return False
        self.last = None
        return True


def find_adb():
    found = shutil.which("adb")
    if found:
        return found
    for sdk in (os.getenv("ANDROID_SDK_ROOT"), os.getenv("ANDROID_HOME"),
                str(Path.home() / "AppData/Local/Android/Sdk"), str(Path.home() / "Android/Sdk")):
        if sdk:
            candidate = Path(sdk) / "platform-tools" / ("adb.exe" if os.name == "nt" else "adb")
            if candidate.is_file():
                return str(candidate)
    raise SystemExit("ADB not found. Set ANDROID_SDK_ROOT or add platform-tools to PATH.")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--serial", help="ADB device serial when multiple devices are connected")
    parser.add_argument("--output", type=Path, default=Path(tempfile.gettempdir()) / "robot-captures")
    args = parser.parse_args()
    adb = [find_adb()] + (["-s", args.serial] if args.serial else [])
    package = "com.qualcomm.ftcrobotcontroller"

    def run(*command):
        return subprocess.run(adb + list(command), capture_output=True, text=True, timeout=4)

    for command in (("shell", "echo", "connected"), ("shell", "run-as", package, "id")):
        result = run(*command)
        if result.returncode:
            raise SystemExit(result.stderr or result.stdout)
    folder = args.output / datetime.datetime.now().strftime("%Y%m%d-%H%M%S-%f")
    folder.mkdir(parents=True)

    def status(message):
        message = time.strftime("%H:%M:%S ") + message
        print(message, flush=True)
        with (folder / "status.txt").open("a") as file:
            file.write(message + "\n")

    status(f"Saving to {folder}. Press Ctrl+C to stop. No robot deployment required.")
    events = queue.Queue()
    heartbeat = Heartbeat()
    capture = 0
    with (folder / "logcat.txt").open("w", encoding="utf-8") as logfile:
        stream = subprocess.Popen(adb + ["logcat", "-v", "threadtime", "-T", "1"],
                                  stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                                  text=True, encoding="utf-8", errors="replace")

        def read():
            for line in stream.stdout:
                logfile.write(line)
                logfile.flush()
                if "Sinister" in line and "notifying that app is alive" in line:
                    events.put(time.monotonic())
            events.put(None)

        reader = threading.Thread(target=read, daemon=True)
        reader.start()
        status("WAITING for a live app heartbeat; keep this terminal open during practice.")
        try:
            while True:
                try:
                    event = events.get(timeout=0.2)
                    if event is None:
                        status("ADB log stream ended. Reconnect and run capture again.")
                        break
                    if heartbeat.last is None:
                        status("ARMED: live heartbeat received.")
                    heartbeat.observe(event)
                except queue.Empty:
                    pass
                if not heartbeat.stalled(time.monotonic()):
                    continue
                capture += 1
                target = folder / f"hang-{capture}"
                target.mkdir()
                status("Heartbeat gap detected; requesting a thread dump. A gap alone does not prove a hang.")
                try:
                    pid = run("shell", "pidof", package).stdout.strip()
                    if not re.fullmatch(r"\d+", pid):
                        status("App process unavailable; logs retained, no signal sent.")
                        continue
                    result = run("shell", "run-as", package, "kill", "-3", pid)
                    (target / "signal.txt").write_text(result.stdout + result.stderr)
                    for index in range(3):
                        time.sleep(0.5)
                        result = run("shell", "cat", "/data/anr/traces.txt")
                        (target / f"threads-{index}.txt").write_text(result.stdout + result.stderr)
                    result = run("pull", "/sdcard/robotControllerLog.txt", str(target / "robotControllerLog.txt"))
                    (target / "pull.txt").write_text(result.stdout + result.stderr)
                    status("Capture saved; waiting for another heartbeat to rearm. Check files for dump availability.")
                except subprocess.TimeoutExpired:
                    status("ADB request timed out; available logs retained. Waiting to rearm.")
        except KeyboardInterrupt:
            status("Stopped by user.")
        finally:
            stream.terminate()
            try:
                stream.wait(timeout=3)
            except subprocess.TimeoutExpired:
                stream.kill()
                stream.wait()
            reader.join(timeout=2)


if __name__ == "__main__":
    main()
