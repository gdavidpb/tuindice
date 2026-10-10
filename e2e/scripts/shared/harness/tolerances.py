"""What the drivers put up with and what they refuse, counted from the driver.log of an attempt (B-7).

A tolerance is something the driver put up with and went on: both drivers write a `[tolerance] <key> <detail>` line for each one
through a single funnel (`DriverLog.tolerate`, in the Kotlin one the line starts with the hour of the log), and iOS also ends its log
with `[tolerances] key=N ...`. A refusal
is the driver saying no to a gesture, a touch, a tap, a typing, a deletion, a submit or a back (it fails the step): both platforms
write every one through a single funnel that marks the line `[refusal] <reason>`, and the summary line of iOS does not count it.
A run that passed by way of a tolerance can then be told from one that did not."""

import re

IOS_SUMMARY = re.compile(r"^\[tolerances\]((?:\s+[\w-]+=\d+)*)\s*$")
# The marker of the funnel (DriverLog.kt, RunConfig.swift), in any position of the line: Android puts the hour before it.
TOLERANCE = re.compile(r"\[tolerance\]\s+([\w-]+)")
# The marker both drivers' refusal funnel writes (DriverLog.kt, RunConfig.swift); the iOS driver's own "[driver]" prefix is not the word.
REFUSAL = re.compile(r"\[refusal\]\s+(?:\[driver\]\s+)?(\S+)")


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
            found = TOLERANCE.search(line)
            if found:
                counts[found.group(1)] = counts.get(found.group(1), 0) + 1
    return {key: n for key, n in counts.items() if n}


def refusals(text):
    """{'<first word of the reason>-refused': n}: the lines are always read, a summary or not."""
    counts = {}
    for line in text.splitlines():
        found = REFUSAL.search(line)
        if found:
            key = "%s-refused" % found.group(1).rstrip(":")
            counts[key] = counts.get(key, 0) + 1
    return counts


def merge(*counts):
    total = {}
    for item in counts:
        for key, n in (item or {}).items():
            total[key] = total.get(key, 0) + n
    return total
