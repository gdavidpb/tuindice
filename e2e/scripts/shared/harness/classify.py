"""Failure classification. First match wins; see plan section 4.5."""

import base64
import difflib
import re

PRODUCT = "product_assertion"
BACKEND = "backend_mismatch"
TIMEOUT = "timeout"
TOOLING = "tooling_error"
TYPED = "typed_text_mismatch"
CRASH = "app_crash"
ENVIRONMENT = "environment"

NON_RETRYABLE = (TYPED, CRASH)
JUNIT_FAILURES = (PRODUCT, TYPED, BACKEND, CRASH)
BOOTSTRAP_PATH = "/auth/v2/bootstrap"
RUNNER_ERROR = re.compile(r"error:|Testing failed|INSTRUMENTATION_FAILED|Process crashed")
# A step line of driver.log, both platforms: `[3] Tap tag:x -> passed (120 ms)`, with a clock prefix on Android.
DRIVER_STEP = re.compile(r"^(?:\d\d:\d\d:\d\d\.\d+\s+)?\[(-?\d+)\]\s+(\S+)(?:\s+(.*?))?\s+->\s+(\w+)")
# What an iOS simulator that stopped serving accessibility and preferences (after hours of runs) writes in the logs of the app and
# of the runner, with the number of lines it takes to say so: measured, a healthy attempt has none of either and a degraded one
# about 1050 and 111, while a single line of the second kind can have another cause. "Couldn't write values for keys" is not
# here on purpose: iOS writes it for a preference outside the app's container.
DEGRADED_MARKERS = {"kAXErrorAPIDisabled": 3, "Couldn't read values in CFPrefsPlistSource": 20}
DEGRADED_TEXT = "the simulator stopped serving accessibility/preferences"
# Failures a degraded simulator is blamed for: the ones the product's own evidence does not decide. A crash report and the text
# the backend received are facts about the product and stay as they are; the requests without a stub are the guess of a rule,
# which a simulator that cannot be read explains as well.
DEGRADABLE = ("product_assertion", "timeout", "tooling_error", "backend_mismatch")


class Evidence:
    """Everything the harness knows about one finished attempt."""

    def __init__(self, scenario, account=None, platform="ios"):
        self.scenario = scenario
        self.account = account
        self.platform = platform
        self.pre_failure = None      # "<step>: <detail>" when health/reset-app/WireMock failed first
        self.crash = {"kind": "none", "excerpt": ""}
        self.killed_after = None     # seconds, when the harness killed the runner
        self.result = None           # parsed result.json
        self.result_error = None     # why result.json is unusable
        self.native_ok = None
        self.tests_executed = None
        self.runner_log = ""
        self.driver_log = ""         # driver.log of the attempt: the only witness of how far a runner that was killed got
        self.logs = {}               # name -> bounded text of the logs collected for a failed attempt (app.log, logcat.txt, ...)
        self.degraded_health = False  # the health verb said the simulator stopped serving preferences
        self.journal = []
        self.backend_down = False    # the journal could not be read and WireMock does not answer: it died during the attempt

    def degradation(self):
        """{marker: lines} over the logs of the attempt when one marker reaches its threshold (iOS only), else None."""
        if self.platform != "ios":
            return None
        texts = list(self.logs.values()) + [self.runner_log]
        counts = {marker: sum(text.count(marker) for text in texts) for marker in DEGRADED_MARKERS}
        return counts if any(counts[m] >= DEGRADED_MARKERS[m] for m in counts) else None


class Classification:
    def __init__(self, klass, summary, note=None, degraded=False, markers=None):
        self.klass = klass       # None means the attempt passed
        self.summary = summary
        self.note = note         # something worth keeping that did not change the verdict
        self.degraded = degraded  # an environment failure caused by a simulator that stopped serving preferences
        self.markers = markers or {}  # the lines of each marker in the logs read, for the record

    @property
    def passed(self):
        return self.klass is None


def _last_logged_step(log):
    """What a killed runner got through, from driver.log: it writes result.json only when it ends, the log line by line."""
    done = [m for m in (DRIVER_STEP.match(line) for line in log.splitlines()) if m and m.group(4) == "passed"]
    if done:
        return "last completed step %s %s(%s)" % (done[-1].group(1), done[-1].group(2), done[-1].group(3) or "")
    return "the driver logged no completed step, so the step it was in is not known" if log.strip() \
        else "the driver wrote no step to its log, so the step it was in is not known"


def _failure_shape(failure):
    """Why the failure of a failed result.json cannot be read, or None. The runners write it with the kit's codec, so this is
    a defect of the tooling when it happens."""
    if not isinstance(failure, dict):
        return "the failure is %s, not an object" % ("missing" if failure is None else type(failure).__name__)
    index = failure.get("stepIndex", -1)
    if isinstance(index, bool) or not isinstance(index, int):
        return "the failure's stepIndex is %r, not a whole number" % (index,)
    return None


def _step_text(failure):
    return "step %s %s(%s)" % (failure.get("stepIndex"), failure.get("primitive", "?"), failure.get("target", ""))


def _diff_indexes(expected, actual, limit=4):
    positions = [i for i in range(max(len(expected), len(actual)))
        if expected[i:i + 1] != actual[i:i + 1]]
    shown = ",".join(str(i) for i in positions[:limit])
    return shown + (",..." if len(positions) > limit else "")


def _header(request, name):
    for key, value in (request.get("headers") or {}).items():
        if key.lower() == name.lower():
            return value[0] if isinstance(value, list) and value else value
    return None


def _status(entry):
    return (entry.get("response") or entry.get("responseDefinition") or {}).get("status")


def _path(entry):
    return (entry.get("request") or {}).get("url", "").split("?")[0]


def _decode_basic(value):
    if not value or not value.startswith("Basic "):
        return None
    try:
        return base64.b64decode(value[6:]).decode("utf-8")
    except (ValueError, UnicodeDecodeError):
        return None


def _expected_credentials(account):
    user, password = account["usbId"], account["password"]
    expected = ["%s:%s" % (user, password)]
    if account.get("backendIdentifier"):  # exported by the catalog when it knows it
        expected.append("%s:%s" % (account["backendIdentifier"], password))
    elif "@" in user:  # otherwise the rule the app applies: the local part of an e-mail
        expected.append("%s:%s" % (user.split("@")[0], password))
    return expected


def _walk(node):
    if isinstance(node, dict):
        yield node
        for value in node.values():
            yield from _walk(value)
    elif isinstance(node, list):
        for value in node:
            yield from _walk(value)


def declared_credentials(scenario, account):
    """Every `user:password` the scenario declares: its account (with the backend identifier rule) and the `basicAuth` of
    each ExpectRequest step, nested ones included. A request that carries any other Basic credential was typed wrong."""
    declared = _expected_credentials(account) if account else []
    declared += [step["basicAuth"] for step in _walk(scenario.raw.get("steps", [])) if isinstance(step.get("basicAuth"), str)]
    return declared


def _request_name(entry):
    request = entry.get("request") or {}
    return "%s %s" % (request.get("method", "?"), _path(entry))


def _typed_verdict(ev):
    """TYPED when any request reached the backend with credentials the scenario does not declare: text typed wrong never
    depends on which endpoint saw it first, and it is never retried."""
    declared = declared_credentials(ev.scenario, ev.account)
    for entry in ev.journal if declared else []:
        decoded = _decode_basic(_header(entry.get("request") or {}, "Authorization"))
        if decoded is None or decoded in declared:
            continue
        user = decoded.split(":", 1)[0]
        same_user = [c for c in declared if c.split(":", 1)[0] == user] or declared
        # The declared credential the received one is closest to is the one that was being typed.
        want = max(same_user, key=lambda c: difflib.SequenceMatcher(None, c, decoded).ratio())
        got = decoded
        if want.split(":", 1)[0] == got.split(":", 1)[0]:
            want, got = want.split(":", 1)[1], got.split(":", 1)[1]
        return Classification(TYPED, "typed '%s' but the backend received '%s' on %s (diff at %s)"
            % (want, got, _request_name(entry), _diff_indexes(want, got)))
    return None


def _journal_verdict(ev, failure):
    typed = _typed_verdict(ev)
    if typed:
        return typed
    account = ev.account
    for entry in ev.journal:
        decoded = _decode_basic(_header(entry.get("request") or {}, "Authorization"))
        if _path(entry) == BOOTSTRAP_PATH and _status(entry) == 401 and decoded is not None:
            return Classification(BACKEND, "the backend answered 401 to the declared credential '%s' on %s at %s: %s; a scenario "
                "that scripts that rejection fails at its own step, so read the step" % (decoded, BOOTSTRAP_PATH, _step_text(failure),
                    failure.get("message", "")))
    unmatched = _unmatched_paths(ev.journal)
    if unmatched:
        return Classification(BACKEND, "requests without a stub: %s" % ", ".join(unmatched))
    if account and account.get("accessToken"):
        for entry in ev.journal:
            path = _path(entry)
            bearer = _header(entry.get("request") or {}, "Authorization")
            if _status(entry) in (401, 403) and not path.startswith("/auth/") and bearer \
                    and bearer.startswith("Bearer ") and bearer[7:] != account["accessToken"]:
                return Classification(BACKEND, "%s answered %s to a Bearer that is not the account's token" % (path, _status(entry)))
    return None


def _unmatched_paths(journal):
    return sorted(set(_path(e) for e in journal if e.get("wasMatched") is False))


def unmatched_summary(journal):
    """What the attempt keeps of the requests that had no stub, green attempts included: the retention may delete the
    journal itself, this stays in the ledger."""
    count = sum(1 for e in journal if e.get("wasMatched") is False)
    return {"count": count, "paths": _unmatched_paths(journal)[:10]}


def classify(ev):
    """Returns a Classification; `klass is None` means the attempt passed. An ANR of another process only reclassifies an
    attempt that failed (and was not a crash or a typing defect); on a passing attempt it is noted and nothing more."""
    verdict = _verdict(ev)
    if ev.backend_down and not verdict.passed and verdict.klass not in (ENVIRONMENT, CRASH, TYPED):
        return Classification(ENVIRONMENT, "WireMock stopped answering during the attempt; it had failed as %s: %s"
            % (verdict.klass, verdict.summary[:160]))
    found = ev.degradation() if verdict.klass in DEGRADABLE else None
    if found:
        counts = ", ".join("%s x%d" % item for item in sorted(found.items()))
        return Classification(ENVIRONMENT, "%s (%s lines in the logs); the attempt had failed as %s: %s"
            % (DEGRADED_TEXT, counts, verdict.klass, verdict.summary[:160]), degraded=True, markers=found)
    anr = ev.crash.get("kind") == "system_anr"
    text = "system ANR: %s" % (ev.crash.get("excerpt") or "").strip()[:200]
    if anr and verdict.passed:
        verdict.note = "ignored, the scenario passed: " + text
    elif anr and verdict.klass not in (ENVIRONMENT,) + NON_RETRYABLE:
        return Classification(ENVIRONMENT, text)
    return verdict


def _verdict(ev):
    if ev.pre_failure:
        return Classification(ENVIRONMENT, ev.pre_failure, degraded=ev.degraded_health)
    if ev.crash.get("kind") in ("app_crash", "app_anr"):
        return Classification(CRASH, "%s: %s" % (ev.crash["kind"], (ev.crash.get("excerpt") or "").strip()[:200]))
    result = ev.result
    if ev.killed_after is not None:
        return Classification(TIMEOUT, "scenario exceeded %ds; %s" % (ev.killed_after, _last_logged_step(ev.driver_log)))
    if result is None:
        matches = [line for line in ev.runner_log.splitlines() if RUNNER_ERROR.search(line)]
        return Classification(TOOLING, ev.result_error or (matches[-1].strip() if matches else "result.json is missing"))
    if result.get("scenarioId") != ev.scenario.id:
        return Classification(TOOLING, "result.json is for %r, expected %r" % (result.get("scenarioId"), ev.scenario.id))
    if ev.tests_executed != 1:
        return Classification(TOOLING, "filter matched %s tests, expected exactly 1" % ev.tests_executed)
    passed = result.get("outcome") == "passed"
    if passed != bool(ev.native_ok):
        return Classification(TOOLING, "runner and result disagree (result %s, runner %s)"
            % (result.get("outcome"), "ok" if ev.native_ok else "failed"))
    if passed:
        return Classification(None, "")
    failure = result.get("failure")
    problem = _failure_shape(failure)
    if problem:
        return Classification(TOOLING, "result.json is malformed: %s" % problem)
    kind = failure.get("kind")
    where = "%s: %s" % (_step_text(failure), failure.get("message", ""))
    if kind == "TYPED_TEXT_MISMATCH":
        # ExpectRequest compares what the backend received; every other step compares what the field held.
        place = "the backend received" if failure.get("primitive") == "ExpectRequest" else "the field held"
        return Classification(TYPED, "%s: typed '%s' but %s '%s'"
            % (_step_text(failure), failure.get("expected", ""), place, failure.get("actual", "")))
    if kind in ("SYSTEM_DIALOG", "BACKEND_UNAVAILABLE"):
        return Classification(ENVIRONMENT, where)
    if kind == "APP_NOT_RUNNING" and int(failure.get("stepIndex", -1)) < 0:
        # Before the first step there is no evidence of a crash: the emulator or simulator
        # was not ready, which is environment. Later, a crash needs crash-probe evidence (the
        # branch above); a driver that merely finds the app gone is a product assertion.
        return Classification(ENVIRONMENT, where)
    # Before the first step (index -1) the journal still holds the previous scenario's requests: it says nothing.
    journal = None if int(failure.get("stepIndex", 0)) < 0 else _journal_verdict(ev, failure)
    if journal:
        return journal
    if kind == "DRIVER_ERROR":
        return Classification(TOOLING, where)
    return Classification(PRODUCT, where)
