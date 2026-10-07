"""Failure classification. First match wins; see plan section 4.5."""

import base64
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


class Evidence:
    """Everything the harness knows about one finished attempt."""

    def __init__(self, scenario, account=None):
        self.scenario = scenario
        self.account = account
        self.pre_failure = None      # "<step>: <detail>" when health/reset-app/WireMock failed first
        self.crash = {"kind": "none", "excerpt": ""}
        self.killed_after = None     # seconds, when the harness killed the runner
        self.result = None           # parsed result.json
        self.result_error = None     # why result.json is unusable
        self.native_ok = None
        self.tests_executed = None
        self.runner_log = ""
        self.journal = []


class Classification:
    def __init__(self, klass, summary):
        self.klass = klass       # None means the attempt passed
        self.summary = summary

    @property
    def passed(self):
        return self.klass is None


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
    if "@" in user:
        expected.append("%s:%s" % (user.split("@")[0], password))
    return expected


def _journal_verdict(ev):
    account = ev.account
    for entry in ev.journal:
        request = entry.get("request") or {}
        decoded = _decode_basic(_header(request, "Authorization"))
        if _path(entry) != BOOTSTRAP_PATH or _status(entry) != 401 or decoded is None or not account:
            continue
        expected = _expected_credentials(account)
        if decoded in expected:
            return Classification(BACKEND, "the mock rejected the declared credential '%s' with 401 on %s" % (decoded, BOOTSTRAP_PATH))
        want, got = expected[0], decoded
        if want.split(":", 1)[0] == got.split(":", 1)[0]:
            want, got = want.split(":", 1)[1], got.split(":", 1)[1]
        return Classification(TYPED, "typed '%s' but the backend received '%s' (diff at %s)"
            % (want, got, _diff_indexes(want, got)))
    unmatched = [_path(e) for e in ev.journal if e.get("wasMatched") is False]
    if unmatched:
        return Classification(BACKEND, "requests without a stub: %s" % ", ".join(sorted(set(unmatched))))
    if account and account.get("accessToken"):
        for entry in ev.journal:
            path = _path(entry)
            bearer = _header(entry.get("request") or {}, "Authorization")
            if _status(entry) in (401, 403) and not path.startswith("/auth/") and bearer \
                    and bearer.startswith("Bearer ") and bearer[7:] != account["accessToken"]:
                return Classification(BACKEND, "%s answered %s to a Bearer that is not the account's token" % (path, _status(entry)))
    return None


def classify(ev):
    """Returns a Classification; `klass is None` means the attempt passed."""
    if ev.pre_failure:
        return Classification(ENVIRONMENT, ev.pre_failure)
    if ev.crash.get("kind") in ("app_crash", "app_anr"):
        return Classification(CRASH, "%s: %s" % (ev.crash["kind"], (ev.crash.get("excerpt") or "").strip()[:200]))
    if ev.crash.get("kind") == "system_anr":
        return Classification(ENVIRONMENT, "system ANR: %s" % (ev.crash.get("excerpt") or "").strip()[:200])
    result = ev.result
    if ev.killed_after is not None:
        steps = [s for s in (result or {}).get("steps", []) if s.get("outcome") == "passed"]
        last = "last completed step %s %s(%s)" % (steps[-1].get("index"), steps[-1].get("primitive"), steps[-1].get("target")) \
            if steps else "no step completed"
        return Classification(TIMEOUT, "scenario exceeded %ds; %s" % (ev.killed_after, last))
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
    failure = result.get("failure") or {}
    kind = failure.get("kind")
    where = "%s: %s" % (_step_text(failure), failure.get("message", ""))
    if kind == "TYPED_TEXT_MISMATCH":
        return Classification(TYPED, "%s: typed '%s' but the field held '%s'"
            % (_step_text(failure), failure.get("expected", ""), failure.get("actual", "")))
    if kind in ("SYSTEM_DIALOG", "BACKEND_UNAVAILABLE"):
        return Classification(ENVIRONMENT, where)
    if kind == "APP_NOT_RUNNING" and int(failure.get("stepIndex", -1)) < 0:
        # Before the first step there is no evidence of a crash: the emulator or simulator
        # was not ready, which is environment. Later, a crash needs crash-probe evidence (the
        # branch above); a driver that merely finds the app gone is a product assertion.
        return Classification(ENVIRONMENT, where)
    journal = _journal_verdict(ev)
    if journal:
        return journal
    if kind == "DRIVER_ERROR":
        return Classification(TOOLING, where)
    return Classification(PRODUCT, where)
