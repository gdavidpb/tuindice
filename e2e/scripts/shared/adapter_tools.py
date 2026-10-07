#!/usr/bin/env python3
"""What the platform adapters read out of tool output. The adapters stay shell; the reading lives here so it is tested.

Usage: adapter_tools.py <command> [args...]. Every command prints one JSON object on stdout, except catalog-field (a bare string).

  apk-outputs <app dir> <test dir>               paths and ids from the output-metadata.json of two APK directories
  catalog-field <catalog> <id> <android|ios>     the runner identifier of a scenario
  instrument-summary <log>                       {nativeOk, testsExecuted} of an `am instrument -r` run
  instrument-tests <log>                         {tests} the `run[<id>]` tests of an `am instrument -e log true` listing
  logcat-crash <log> <since> <app id> <file>     {kind, excerpt} from `logcat -v epoch`; the evidence goes to <file>
  xctest-summary <log> <exit code>               {nativeOk, testsExecuted} of an xcodebuild test run
  xctest-tests <enumeration json>                {tests} the `Target/Class/method` identifiers of an enumeration
  ios-crash <reports dir> <since> <process> <file>  {kind, excerpt} from the crash reports of the app process
"""

import json
import os
import re
import sys

INSTRUMENT_CODE = re.compile(r"^INSTRUMENTATION_STATUS_CODE: (-?\d+)", re.M)
INSTRUMENT_TEST = re.compile(r"^INSTRUMENTATION_STATUS: test=run\[(.+)\]\s*$", re.M)
LOGCAT_LINE = re.compile(r"^\s*(\d+)\.\d+\s+\d+\s+\d+\s+[A-Z]\s+(\S+)\s*:\s?(.*)$")
EXCERPT_LINES = 40


def _read(path):
    with open(path, errors="replace") as handle:
        return handle.read()


def apk_outputs(app_dir, test_dir):
    found = {}
    for key, directory in (("app", app_dir), ("test", test_dir)):
        metadata = json.loads(_read(os.path.join(directory, "output-metadata.json")))
        apks = [e["outputFile"] for e in metadata["elements"] if e.get("outputFile", "").endswith(".apk")]
        if len(apks) != 1:
            raise SystemExit("%s lists %d APKs, expected exactly 1" % (directory, len(apks)))
        path = os.path.join(directory, apks[0])
        if not os.path.isfile(path):
            raise SystemExit("the build did not produce %s" % path)
        found[key], found[key + "Id"] = path, metadata["applicationId"]
    return found


def catalog_field(catalog, scenario, platform):
    for entry in json.loads(_read(catalog))["scenarios"]:
        if entry["id"] == scenario:
            return entry[platform]["scenarioArg" if platform == "android" else "onlyTesting"]
    raise SystemExit("scenario %s is not in %s" % (scenario, catalog))


def instrument_summary(log):
    text = _read(log)
    codes = [int(c) for c in INSTRUMENT_CODE.findall(text)]
    finished = [c for c in codes if c != 1]  # 1 is "started"; the others close a test
    broken = "INSTRUMENTATION_FAILED" in text or "Process crashed" in text
    ok = len(finished) == 1 and finished[0] == 0 and not broken and "INSTRUMENTATION_CODE: -1" in text
    return {"nativeOk": ok, "testsExecuted": len(finished)}


def instrument_tests(log):
    """A listing run reports each test twice: a status block with code 1 when it starts, then one that closes it."""
    tests, current = [], None
    for line in _read(log).splitlines():
        match = INSTRUMENT_TEST.match(line)
        if match:
            current = match.group(1)
        elif line.startswith("INSTRUMENTATION_STATUS_CODE: 1") and current is not None:
            tests.append(current)
            current = None
    return {"tests": tests}


def _logcat_events(text, since):
    for line in text.splitlines():
        match = LOGCAT_LINE.match(line)
        if match and int(match.group(1)) >= since:
            yield line


def logcat_crash(log, since, app_id, evidence_file):
    """Kinds: app_crash (Java or native, of the app), app_anr, system_anr (an ANR of any other process), none."""
    lines = list(_logcat_events(_read(log), int(since)))
    kind, at = "none", 0
    for index, line in enumerate(lines):
        if "FATAL EXCEPTION" in line and any(("Process: %s," % app_id) in l or ("Process: %s" % app_id) in l
                for l in lines[index:index + 4]):
            kind, at = "app_crash", index
        elif re.search(r">>> %s <<<" % re.escape(app_id), line) and "Fatal signal" in "".join(lines[max(0, index - 3):index + 1]):
            kind, at = "app_crash", index
        elif "ANR in " in line:
            culprit = line.split("ANR in ", 1)[1].split()[0].rstrip(",")
            kind, at = ("app_anr" if culprit == app_id else "system_anr"), index
        if kind != "none":
            break
    if kind == "none":
        return {"kind": "none", "excerpt": ""}
    excerpt = lines[max(0, at - 1):at + EXCERPT_LINES]
    with open(evidence_file, "w") as handle:
        handle.write("\n".join(excerpt) + "\n")
    return {"kind": kind, "excerpt": "\n".join(line.strip() for line in excerpt[:3])}


def xctest_summary(log, exit_code):
    text = _read(log)
    executed = [int(n) for n in re.findall(r"Executed (\d+) tests?", text)]
    # xcodebuild prints the count per suite and a final total; the largest is the total.
    return {"nativeOk": int(exit_code) == 0 and "TEST EXECUTE FAILED" not in text, "testsExecuted": max(executed or [0])}


def xctest_tests(path):
    """`-enumerate-tests -test-enumeration-format json` lists identifiers as `Target/Class/test_name()`;
    the catalog's onlyTesting has no parentheses. A class without methods (the base case) is listed as a bare entry."""
    names = []
    for plan in json.loads(_read(path)).get("values", []):
        names.extend(item["identifier"][:-2] if item["identifier"].endswith("()") else item["identifier"]
            for item in plan.get("enabledTests", []))
    return {"tests": names}


def ios_crash(reports_dir, since, process, evidence_file):
    newest = None
    for name in sorted(os.listdir(reports_dir)) if os.path.isdir(reports_dir) else []:
        path = os.path.join(reports_dir, name)
        if name.startswith(process) and name.endswith((".ips", ".crash")) and os.path.getmtime(path) >= int(since):
            newest = path
    if not newest:
        return {"kind": "none", "excerpt": ""}
    text = _read(newest)
    with open(evidence_file, "w") as handle:
        handle.write("# %s\n%s" % (newest, text[:20000]))
    reason = re.search(r'"(?:exception|termination)"\s*:\s*\{[^}]*\}', text)
    return {"kind": "app_crash", "excerpt": "%s: %s" % (os.path.basename(newest), (reason.group(0) if reason else text[:200]))[:400]}


COMMANDS = {
    "apk-outputs": (apk_outputs, 2), "catalog-field": (catalog_field, 3), "instrument-summary": (instrument_summary, 1),
    "instrument-tests": (instrument_tests, 1), "logcat-crash": (logcat_crash, 4), "xctest-summary": (xctest_summary, 2),
    "xctest-tests": (xctest_tests, 1), "ios-crash": (ios_crash, 4),
}


def main(argv):
    if len(argv) < 2 or argv[1] not in COMMANDS:
        sys.stderr.write(__doc__)
        return 64
    function, arity = COMMANDS[argv[1]]
    if len(argv) - 2 != arity:
        sys.stderr.write("%s takes %d arguments\n" % (argv[1], arity))
        return 64
    result = function(*argv[2:])
    print(result if isinstance(result, str) else json.dumps(result))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
