"""What the drivers tolerate on their own, counted from the driver.log of an attempt (B-7).

iOS ends its log with `[tolerances] key=N ...` and writes a `[tolerance] <key> <detail>` line for each one; Android writes one
line per time it brings the app back to the front or refuses a gesture. A run that passed by way of a tolerance can then be told
from one that did not."""

import re

TAIL_BYTES = 4 * 1024 * 1024
IOS_SUMMARY = re.compile(r"^\[tolerances\]((?:\s+[\w-]+=\d+)*)\s*$")
IOS_LINE = re.compile(r"^\[tolerance\]\s+([\w-]+)")
# (key, pattern) of the lines the Android driver writes (scenariorunner/driver: AppLauncher, DeviceSession, KeyboardGuard, GestureInjector).
ANDROID_LINES = (
    ("foreground-request", re.compile(r"foreground: .* request \d+ to bring the app back")),
    ("foreground-not-in-front", re.compile(r"foreground: not in front after")),
    ("gesture-refused", re.compile(r"(?:gesture|touch) refused")),
)


def count(text):
    """{key: n} of the tolerances in a driver log; keys with no occurrence are left out, so none at all is an empty object."""
    lines = text.splitlines()
    summary = [IOS_SUMMARY.match(line) for line in lines]
    summary = [m for m in summary if m]
    if summary:  # iOS finished its log: the count it kept is the one to read
        counts = {key: int(n) for key, n in (pair.split("=") for pair in summary[-1].group(1).split())}
    else:
        counts = {}
        for line in lines:
            ios = IOS_LINE.match(line)
            if ios:
                counts[ios.group(1)] = counts.get(ios.group(1), 0) + 1
                continue
            for key, pattern in ANDROID_LINES:
                if pattern.search(line):
                    counts[key] = counts.get(key, 0) + 1
    return {key: n for key, n in counts.items() if n}


def count_file(path):
    """count() of the last TAIL_BYTES of a file; a file that does not exist has none."""
    try:
        with open(path, "rb") as handle:
            handle.seek(0, 2)
            handle.seek(max(0, handle.tell() - TAIL_BYTES))
            return count(handle.read().decode("utf-8", errors="replace"))
    except OSError:
        return {}


def merge(*counts):
    total = {}
    for item in counts:
        for key, n in (item or {}).items():
            total[key] = total.get(key, 0) + n
    return total
