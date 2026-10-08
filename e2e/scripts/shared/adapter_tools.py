#!/usr/bin/env python3
"""What the platform adapters read out of tool output. The adapters stay shell; the reading lives here so it is tested.

Usage: adapter_tools.py <command> [args...]. Every command prints one JSON object on stdout, except catalog-field (a bare string).

  apk-outputs <app dir> <test dir>               paths and ids from the output-metadata.json of two APK directories
  catalog-field <catalog> <id> <android|ios>     the runner identifier of a scenario
  instrument-summary <log>                       {nativeOk, testsExecuted} of an `am instrument -r` run
  instrument-tests <log>                         {tests} the `run[<id>]` tests of an `am instrument -e log true` listing
  logcat-crash <log> <since> <app id> <file>     {kind, excerpt} from `logcat -v epoch`; the evidence goes to <file>
  logcat-window <log> <since> <max bytes> <file> {bytes, truncated} the `logcat -v epoch` lines from <since> on, the last <max bytes> of them
  cap-log <max bytes> <file>                     {bytes, truncated} stdin to <file>, the last <max bytes> of it
  instrument-probes <log> <required> <result> <artifacts>   {ok, passed, failed, skipped, artifacts} of an `am instrument -r` run of several classes. Every test of
                                                 the run must pass except the measurements (MEASUREMENTS); <required> is a comma list of
                                                 `Class#method` that must pass and of `Class` that must have a passing test, <result> the
                                                 contract's result.json
  xctest-probes <log> <exit code> <result> <artifacts> <classes>  like xctest-contract, and the comma-separated classes of probes
                                                 the same run executes must each show a passed test and no failed one
  xctest-contract <log> <exit code> <result> <artifacts>   {ok, passed, failed, skipped, artifacts} of the xcodebuild run of the contract test
  app-may-have-crashed <result.json>             yes | no: whether the attempt can have left a crash report (iOS waits for it only then)
  xctest-summary <log> <exit code>               {nativeOk, testsExecuted} of an xcodebuild test run
  xctest-tests <enumeration json>                {tests} the `Target/Class/method` identifiers of an enumeration
  ios-crash <reports dir> <since> <until> <process> <file> <udid> <wait s>  {kind, excerpt} from the crash report of the app
                                                 process captured in [since, until] on that simulator, waiting up to <wait s> for it
"""

import datetime
import json
import os
import re
import sys
import time

INSTRUMENT_CODE = re.compile(r"^INSTRUMENTATION_STATUS_CODE: (-?\d+)", re.M)
INSTRUMENT_TEST = re.compile(r"^INSTRUMENTATION_STATUS: test=run\[(.+)\]\s*$", re.M)
LOGCAT_LINE = re.compile(r"^\s*(\d+)\.\d+\s+\d+\s+\d+\s+[A-Z]\s+(\S+)\s*:\s?(.*)$")
LOGCAT_STAMP = re.compile(rb"^\s*(\d+)\.\d+\s")
EXCERPT_LINES = 40
# Room the first line of a capped log may use, counted inside the cap.
MARKER_BUDGET = 160


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


STATUS_KEY = re.compile(r"^INSTRUMENTATION_STATUS: (class|test)=(.*)$")
STATUS_CODE = re.compile(r"^INSTRUMENTATION_STATUS_CODE: (-?\d+)")
# The code that closes a test in an `am instrument -r` stream: 0 passed, -2 failed, -3 ignored, -4 assumption not met (skipped).
CLOSING = {0: "passed", -2: "failed", -3: "skipped", -4: "skipped"}
# The tests that measure and do not gate: skipped by design unless asked for with an argument. Any other skipped test is red.
MEASUREMENTS = ("AndroidTypingProbesTest#typingSeries",)


def _contract_steps(result):
    """([passed names], [failed names], problem) of the steps of the contract's result.json; a result that cannot be read is a problem."""
    try:
        steps = json.loads(_read(result)).get("steps")
        if not isinstance(steps, list) or not steps:
            return [], [], "result.json has no steps"
        names = [(str(s.get("primitive")), s.get("outcome") == "passed") for s in steps]
    except (OSError, ValueError, AttributeError) as error:
        return [], [], "result.json is unreadable: %s" % error
    return [n for n, ok in names if ok], [n for n, ok in names if not ok], None


def instrument_probes(log, required, result, artifacts=None):
    """Every test of an `am instrument -r` run, as `Class#method`, sorted by how it closed. The run is red when a test failed, was
    skipped (a measurement apart), started and did not close, or closed with a status code this reader does not know; when the
    process died or the stream does not end with INSTRUMENTATION_CODE: -1; when a required test or class did not pass; or when
    nothing ran. The steps of the contract's own result.json are listed as `contract:<check>`."""
    text, current, started, closed, passed, failed, skipped = _read(log), {}, [], [], [], [], []
    for line in text.splitlines():
        key = STATUS_KEY.match(line)
        code = STATUS_CODE.match(line)
        if key:
            current[key.group(1)] = key.group(2).strip()
        elif code:
            number = int(code.group(1))
            if "test" in current:
                name = "%s#%s" % (current.get("class", "?").rsplit(".", 1)[-1], current["test"])
                if number == 1:
                    started.append(name)
                elif number in CLOSING:
                    closed.append(name)
                    {"passed": passed, "failed": failed, "skipped": skipped}[CLOSING[number]].append(name)
                else:
                    closed.append(name)
                    failed.append("%s: closed with the status code %d, which this reader does not know" % (name, number))
            current = {}
    failed += ["%s: did not finish" % name for name in started if name not in closed]
    steps_passed, steps_failed, problem = _contract_steps(result)
    passed += ["contract:" + n for n in steps_passed]
    failed += ["contract:" + n for n in steps_failed]
    if problem:
        failed.append("contract:" + problem)
    if "INSTRUMENTATION_FAILED" in text or "Process crashed" in text:
        failed.append("instrumentation: the run failed or the process crashed")
    if "INSTRUMENTATION_CODE: -1" not in text:
        failed.append("instrumentation: the run did not end with INSTRUMENTATION_CODE: -1")
    owed = [name for name in required.split(",") if name and "#" in name and name not in passed] \
        + [name for name in skipped if name not in MEASUREMENTS]
    failed += ["%s: did not pass (%s)" % (name, "skipped" if name in skipped else "not run") for name in dict.fromkeys(owed)]
    failed += ["%s: no test of the class passed" % name for name in required.split(",")
        if name and "#" not in name and not any(p.startswith(name + "#") for p in passed)]
    return _answer(not failed and bool(passed), passed, failed, skipped, artifacts)


def _answer(ok, passed, failed, skipped, artifacts):
    answer = {"ok": ok, "passed": passed, "failed": failed, "skipped": skipped}
    return dict(answer, artifacts=artifacts) if artifacts is not None else answer


TEST_CASE = re.compile(r"^Test Case '-\[(?:\S+\.)?(\w+) (\w+)\]' (passed|failed)", re.M)


def xctest_contract(log, exit_code, result, artifacts=None, required=""):
    """The contract test of iOS. With [required] (comma-separated classes) the same xcodebuild run also executes those classes of
    probes (the rule of stillness, the Objective-C shim): each must show a passed test case and none may fail. Without it the run
    must be exactly the one contract test."""
    summary = xctest_summary(log, exit_code)
    steps_passed, steps_failed, problem = _contract_steps(result)
    failed = list(steps_failed) + ([problem] if problem else [])
    if not summary["nativeOk"] and not failed:
        failed.append("xcodebuild: the contract test did not pass (exit %s)" % exit_code)
    classes = [name for name in required.split(",") if name]
    if not classes and summary["testsExecuted"] != 1:
        failed.append("xcodebuild: %d tests executed, expected exactly 1" % summary["testsExecuted"])
    cases = TEST_CASE.findall(_read(log))
    failed += ["probes:%s#%s failed" % (k, t) for k, t, outcome in cases if outcome == "failed" and k in classes]
    failed += ["probes:%s: no test of the class passed" % name for name in classes
        if not any(k == name and outcome == "passed" for k, _, outcome in cases)]
    if classes and summary["testsExecuted"] < len(classes):
        failed.append("xcodebuild: %d tests executed for %d classes" % (summary["testsExecuted"], len(classes)))
    return _answer(not failed and bool(steps_passed), steps_passed, failed, [], artifacts)


def app_may_have_crashed(result_path):
    """"yes" when the attempt can have left a crash report: its result.json is unreadable or not an object (the runner died or was
    killed) or the scenario failed because the app was not running; "no" when it failed with the app alive."""
    try:
        result = json.loads(_read(result_path))
    except (OSError, ValueError):
        return "yes"
    failure = result.get("failure") if isinstance(result, dict) else None
    return "yes" if not isinstance(result, dict) or (isinstance(failure, dict) and failure.get("kind") == "APP_NOT_RUNNING") else "no"


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
    """Kinds: app_crash (Java or native, of the app), app_anr, system_anr (an ANR of any other process), none. The whole
    log is read: a crash or ANR of the app wins over an ANR of another process, whichever came first."""
    lines = list(_logcat_events(_read(log), int(since)))
    found = {}
    for index, line in enumerate(lines):
        if "FATAL EXCEPTION" in line and any(("Process: %s," % app_id) in l or ("Process: %s" % app_id) in l
                for l in lines[index:index + 4]):
            found.setdefault("app_crash", index)
        elif re.search(r">>> %s <<<" % re.escape(app_id), line) and "Fatal signal" in "".join(lines[max(0, index - 3):index + 1]):
            found.setdefault("app_crash", index)
        elif "ANR in " in line:
            culprit = line.split("ANR in ", 1)[1].split()[0].rstrip(",")
            found.setdefault("app_anr" if culprit == app_id else "system_anr", index)
    kind = next((k for k in ("app_crash", "app_anr", "system_anr") if k in found), "none")
    if kind == "none":
        return {"kind": "none", "excerpt": ""}
    at = found[kind]
    excerpt = lines[max(0, at - 1):at + EXCERPT_LINES]
    with open(evidence_file, "w") as handle:
        handle.write("\n".join(excerpt) + "\n")
    return {"kind": kind, "excerpt": "\n".join(line.strip() for line in excerpt[:3])}


def _write_capped(path, data, max_bytes, what, total=None):
    """`data` (bytes, the end of an input `total` bytes long) to `path`, at most `max_bytes` long including a first
    line that says what was dropped. The end is what a failure leaves, so the end is what stays; the cut falls on a
    line boundary."""
    max_bytes = int(max_bytes)
    budget = max_bytes - MARKER_BUDGET
    total = len(data) if total is None else total
    truncated = total > max_bytes
    if truncated:
        tail = data[-budget:]
        newline = tail.find(b"\n")
        tail = tail[newline + 1:] if newline >= 0 else b""
        note = "# e2e: truncated, kept the last %d of %d bytes of %s\n" % (len(tail), total, what)
        data = note.encode()[:MARKER_BUDGET - 1].rstrip(b"\n") + b"\n" + tail
    elif not data:
        data = ("# e2e: %s has no lines\n" % what).encode()[:MARKER_BUDGET]
    with open(path, "wb") as handle:
        handle.write(data)
    return {"bytes": len(data), "truncated": truncated}


def logcat_window(log, since, max_bytes, output):
    """The lines of a `logcat -v epoch` dump stamped at or after `since` (a line without a stamp follows its predecessor)."""
    kept, inside = [], False
    with open(log, "rb") as handle:
        for line in handle:
            match = LOGCAT_STAMP.match(line)
            if match:
                inside = int(match.group(1)) >= int(since)
            if inside:
                kept.append(line)
    return _write_capped(output, b"".join(kept), max_bytes, "the log since %s" % since)


def cap_log(max_bytes, output):
    """stdin to `output`, holding only the last `max_bytes` of it in memory while it reads."""
    keep, total = bytearray(), 0
    while True:
        chunk = sys.stdin.buffer.read(65536)
        if not chunk:
            break
        total += len(chunk)
        keep += chunk
        if len(keep) > 2 * int(max_bytes):
            del keep[:-int(max_bytes)]
    return _write_capped(output, bytes(keep), max_bytes, "the log", total)


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


REPORT_TIME = re.compile(r'"captureTime"\s*:\s*"(\d{4}-\d\d-\d\d \d\d:\d\d:\d\d)(?:\.\d+)? ([+-]\d{4})"|'
    r'^Date/Time:\s+(\d{4}-\d\d-\d\d \d\d:\d\d:\d\d)(?:\.\d+)? ([+-]\d{4})', re.M)
# A report written this long after the attempt ended still belongs to it when its capture time is inside the attempt.
IOS_CRASH_POLL_SECONDS = 1.0


def _captured_at(text, fallback):
    """The epoch second a crash report says the crash happened (its own clock, not the file's); `fallback` if it has none."""
    match = REPORT_TIME.search(text)
    if not match:
        return fallback
    stamp, zone = (match.group(1), match.group(2)) if match.group(1) else (match.group(3), match.group(4))
    moment = datetime.datetime.strptime(stamp + zone, "%Y-%m-%d %H:%M:%S%z")
    return moment.timestamp()


def _ios_crash_report(reports_dir, since, until, process, udid):
    best = None
    for name in sorted(os.listdir(reports_dir)) if os.path.isdir(reports_dir) else []:
        path = os.path.join(reports_dir, name)
        if not (name.startswith(process) and name.endswith((".ips", ".crash"))):
            continue
        text = _read(path)
        captured = _captured_at(text, os.path.getmtime(path))
        # Attributed by when the crash happened, never by when the file was written, and only if it is the dedicated simulator's.
        if since <= captured <= until and (not udid or udid in text) and (best is None or captured >= best[0]):
            best = (captured, path, text)
    return best


def ios_crash(reports_dir, since, until, process, evidence_file, udid="", wait="0"):
    """The crash report of the app process whose capture time lies in [since, until] and that names the dedicated simulator.
    The report lands some seconds after the crash, so `wait` seconds are spent polling for it (the harness asks for that
    only when the attempt failed)."""
    since, until, deadline = float(since), float(until), time.monotonic() + float(wait)
    found = _ios_crash_report(reports_dir, since, until, process, udid)
    while not found and time.monotonic() < deadline:
        time.sleep(IOS_CRASH_POLL_SECONDS)
        found = _ios_crash_report(reports_dir, since, until, process, udid)
    if not found:
        return {"kind": "none", "excerpt": ""}
    _captured, newest, text = found
    with open(evidence_file, "w") as handle:
        handle.write("# %s\n%s" % (newest, text[:20000]))
    reason = re.search(r'"(?:exception|termination)"\s*:\s*\{[^}]*\}', text)
    return {"kind": "app_crash", "excerpt": "%s: %s" % (os.path.basename(newest), (reason.group(0) if reason else text[:200]))[:400]}


COMMANDS = {
    "apk-outputs": (apk_outputs, 2), "catalog-field": (catalog_field, 3), "instrument-summary": (instrument_summary, 1),
    "instrument-tests": (instrument_tests, 1), "logcat-crash": (logcat_crash, 4), "logcat-window": (logcat_window, 4), "cap-log": (cap_log, 2),
    "xctest-summary": (xctest_summary, 2), "instrument-probes": (instrument_probes, 4), "xctest-contract": (xctest_contract, 4), "xctest-probes": (xctest_contract, 5),
    "app-may-have-crashed": (app_may_have_crashed, 1),
    "xctest-tests": (xctest_tests, 1), "ios-crash": (ios_crash, 7),
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
