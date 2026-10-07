#!/usr/bin/env python3
"""Fake platform adapter: implements every verb of the adapter protocol without devices.

Usage: fake_adapter.py <platform> <verb> [args...]

Environment: E2E_FAKE_SCRIPT (JSON), E2E_FAKE_STATE (dir), E2E_FAKE_CALL_LOG (file),
E2E_FAKE_WIREMOCK_DIR (dir), E2E_CATALOG_FILE, E2E_CURRENT_SCENARIO (set by the harness).

The script maps scenario ids ("<platform>:<id>" wins over "<id>") to one behaviour per
attempt; the last one repeats. A behaviour is a string or {"do": ..., "journal": ...}:
pass, fail:assertion, fail:typed, fail:driver, fail:app-not-running, fail:app-not-running-late,
fail:dialog, hang, crash, no-result, disagree, zero-tests, env, system-anr, wiremock-down.
`slow` maps "<platform>:<verb>" to seconds slept after the call is logged; `failVerbs` lists the "<platform>:<verb>" that exit 1.
"""

import json
import os
import subprocess
import sys
import time

HERE = os.path.dirname(os.path.abspath(__file__))
FAILURES = {
    "fail:assertion": ("ASSERTION", 3, "WaitVisible", "tag:record_container", "not visible after 5000ms"),
    "fail:typed": ("TYPED_TEXT_MISMATCH", 1, "EnterText", "tag:auth_password_text_field", "typed text differs"),
    "fail:driver": ("DRIVER_ERROR", 2, "Tap", "tag:auth_sign_in_button", "the driver lost the element"),
    "fail:app-not-running": ("APP_NOT_RUNNING", -1, "launch", "", "the app did not launch"),
    "fail:app-not-running-late": ("APP_NOT_RUNNING", 2, "Tap", "tag:x", "the app is gone"),
    "fail:dialog": ("SYSTEM_DIALOG", 1, "Tap", "tag:x", "a system dialog is in front"),
}
CRASH_KINDS = {"crash": "app_crash", "system-anr": "system_anr"}


def load_script():
    path = os.environ.get("E2E_FAKE_SCRIPT")
    return json.load(open(path)) if path else {}


def behaviour(script, platform, scenario, n):
    options = script.get("behaviours", {})
    steps = options.get("%s:%s" % (platform, scenario)) or options.get(scenario) or [script.get("default", "pass")]
    chosen = steps[min(n, len(steps)) - 1]
    return chosen if isinstance(chosen, dict) else {"do": chosen}


def state_path(platform, name):
    directory = os.path.join(os.environ["E2E_FAKE_STATE"], platform)
    os.makedirs(directory, exist_ok=True)
    return os.path.join(directory, name)


def result_json(scenario, failure=None, passed=True):
    return {
        "scenarioId": scenario, "outcome": "passed" if passed else "failed",
        "startedAt": "2026-01-01T00:00:00Z", "finishedAt": "2026-01-01T00:00:01Z",
        "steps": [{"index": 0, "primitive": "WaitVisible", "target": "tag:a", "durationMs": 5, "outcome": "passed"},
                  {"index": 1, "primitive": "Tap", "target": "tag:b", "durationMs": 7, "outcome": "passed"}],
        "failure": failure,
    }


def emit(payload, code=0):
    print(json.dumps(payload))
    sys.exit(code)


def catalog_tests(platform):
    data = json.load(open(os.environ["E2E_CATALOG_FILE"]))
    key = "android" if platform == "android" else "ios"
    tests = []
    for scenario in data["scenarios"]:
        if platform in scenario["platforms"]:
            tests.append(scenario[key]["scenarioArg" if platform == "android" else "onlyTesting"])
    return tests


def run_scenario(platform, scenario, directory, current):
    os.makedirs(directory, exist_ok=True)
    wiremock = os.environ.get("E2E_FAKE_WIREMOCK_DIR")
    journal = {"requests": []}
    if current.get("journal"):
        journal = json.load(open(os.path.join(HERE, "fixtures", "journals", current["journal"] + ".json")))
    if wiremock:
        json.dump(journal, open(os.path.join(wiremock, "journal.json"), "w"))
    do = current["do"]
    with open(os.path.join(directory, "runner.log"), "w") as log:
        log.write("fake runner for %s\n" % scenario)
        if do == "no-result":
            log.write("error: Testing failed: fake runner crashed before writing result.json\n")
    if do == "hang":
        child = subprocess.Popen(["sleep", "300"])
        open(state_path(platform, "hang.pid"), "w").write(str(child.pid))
        time.sleep(300)
    if do == "no-result":
        emit({"nativeOk": False, "testsExecuted": 1}, 1)
    if do in FAILURES:
        kind, step, primitive, target, message = FAILURES[do]
        failure = {"kind": kind, "stepIndex": step, "primitive": primitive, "target": target, "message": message,
                   "expected": "123456", "actual": "12456", "site": {"file": "Fake.kt", "line": 1}}
        json.dump(result_json(scenario, failure, False), open(os.path.join(directory, "result.json"), "w"))
        emit({"nativeOk": False, "testsExecuted": 1}, 1)
    if do in CRASH_KINDS:
        failure = {"kind": "ASSERTION", "stepIndex": 1, "primitive": "Tap", "target": "tag:b", "message": "app vanished"}
        json.dump(result_json(scenario, failure, False), open(os.path.join(directory, "result.json"), "w"))
        emit({"nativeOk": False, "testsExecuted": 1}, 1)
    json.dump(result_json(scenario), open(os.path.join(directory, "result.json"), "w"))
    if do == "disagree":
        emit({"nativeOk": False, "testsExecuted": 1}, 1)
    emit({"nativeOk": True, "testsExecuted": 0 if do == "zero-tests" else 1})


def main(argv):
    platform, verb, args = argv[1], argv[2], argv[3:]
    with open(os.environ["E2E_FAKE_CALL_LOG"], "a") as log:
        log.write(" ".join([platform, verb] + args) + "\n")
    script = load_script()
    key = "%s:%s" % (platform, verb)
    time.sleep(script.get("slow", {}).get(key, 0))
    if key in script.get("failVerbs", []):
        sys.stderr.write("fake %s failed\n" % key)
        sys.exit(1)
    scenario = os.environ.get("E2E_CURRENT_SCENARIO", "")
    wiremock = os.environ.get("E2E_FAKE_WIREMOCK_DIR")
    if verb == "health":
        counter = state_path(platform, scenario + ".n")
        n = int(open(counter).read()) + 1 if os.path.exists(counter) else 1
        open(counter, "w").write(str(n))
        current = behaviour(script, platform, scenario, n)
        json.dump(current, open(state_path(platform, scenario + ".current"), "w"))
        if current["do"] == "env":
            sys.stderr.write("fake device is not responding\n")
            sys.exit(3)
        if current["do"] == "wiremock-down" and wiremock:
            open(os.path.join(wiremock, "down"), "w").close()
        emit({"ok": True})
    current = json.load(open(state_path(platform, scenario + ".current"))) \
        if os.path.exists(state_path(platform, scenario + ".current")) else {"do": "pass"}
    if verb == "run-scenario":
        run_scenario(platform, args[0], args[1], current)
    elif verb == "crash-probe":
        kind = CRASH_KINDS.get(current["do"], "none")
        if kind != "none":
            open(os.path.join(args[1], "crash.txt"), "w").write("FATAL EXCEPTION fake\n")
        emit({"kind": kind, "excerpt": "FATAL EXCEPTION fake" if kind != "none" else ""})
    elif verb == "enumerate":
        tests = catalog_tests(platform)
        extra = script.get("enumerate", {})
        tests = [t for t in tests if t not in extra.get("missing", [])] + list(extra.get("duplicate", []))
        emit({"tests": tests})
    elif verb == "recover":
        if wiremock and os.path.exists(os.path.join(wiremock, "down")):
            os.remove(os.path.join(wiremock, "down"))
        sys.exit(3 if script.get("recover_fails") else 0)
    elif verb == "collect-failure":
        open(os.path.join(args[0], "collect-failure.txt"), "w").write("collected\n")
    elif verb == "ensure-device":
        emit({"id": "fake-1", "model": "Fake", "bootedAt": "2026-01-01T00:00:00Z", "bootedByHarness": True,
              "dataDirGb": 0.1, "settings": {}})
    elif verb == "build":
        emit({"artifacts": ["fake-app", "fake-runner"]})
    elif verb == "toolchain":
        keys = dict(line.split("=", 1) for line in open(os.path.join("e2e", "toolchain", platform + ".lock")).read().splitlines()
            if line and not line.startswith("#"))
        keys.update(script.get("toolchain", {}).get(platform, {}))
        emit(keys)
    elif verb in ("install", "reset-app", "stop-device"):
        emit({})
    else:
        sys.stderr.write("unknown verb %s\n" % verb)
        sys.exit(64)


if __name__ == "__main__":
    main(sys.argv)
