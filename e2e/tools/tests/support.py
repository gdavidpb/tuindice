"""Test kit: a throwaway git repository, fixture catalogs and a runner for e2e.py."""

import glob
import json
import os
import shlex
import shutil
import subprocess
import sys
import tempfile
import unittest

TESTS = os.path.dirname(os.path.abspath(__file__))
E2E_PY = os.path.join(TESTS, "..", "..", "scripts", "shared", "e2e.py")
FAKE_ADAPTER = os.path.join(TESTS, "fake_adapter.py")
FAKE_GH = os.path.join(TESTS, "fake_bin", "gh")
SHARED = os.path.join(TESTS, "..", "..", "scripts", "shared")
sys.path.insert(0, SHARED)


def requires_macos(reason):
    """Skips a test off macOS, with the reason in the message. A test that needs a tool only a Mac has (`defaults`,
    `xcrun`, the iOS build) must say so here instead of passing silently or failing on a Linux runner; the macOS job
    runs them all (run-harness-tests.sh fails there if anything was skipped)."""
    if not reason or not reason.strip():
        raise ValueError("a macOS-only test needs a reason")
    return unittest.skipUnless(sys.platform == "darwin", "macOS only: %s" % reason.strip())


FP_A = "a" * 64
FP_B = "b" * 64

ACCOUNTS = [
    {"id": "canonical", "usbId": "11-11111", "password": "123456", "accessToken": "canon.token",
     "refreshToken": "r", "sessionId": "s", "mockScenario": "login-token-lifecycle"},
    {"id": "record-retry", "usbId": "11-11111", "password": "record-retry-pass", "accessToken": None,
     "refreshToken": None, "sessionId": None, "mockScenario": None},
    {"id": "update-password", "usbId": "55-55555", "password": "outdated-pass", "accessToken": None,
     "refreshToken": None, "sessionId": None, "mockScenario": None},
]


def expect_request(basic_auth, path="/auth/v2/bootstrap"):
    return {"type": "expectRequest", "method": "POST", "path": path, "basicAuth": basic_auth, "timeoutMs": 20000}


def scenario(sid, timeout=30, platforms=("android", "ios"), account=None, tags=(), quarantine=None, steps=()):
    module = sid.split("-")[0]
    return {
        "id": sid, "module": module, "tags": list(tags), "platforms": list(platforms), "account": account,
        "signsIn": bool(account), "timeoutSeconds": timeout, "stepsHash": "h-" + sid, "start": {}, "steps": list(steps),
        "quarantine": quarantine, "covers": [],
        "ios": {"onlyTesting": "TuIndiceUITests/%s/test_%s" % (module, sid.replace("-", "_"))},
        "android": {"scenarioArg": sid},
    }


def catalog(*scenarios):
    return {"schema": "tuindice-e2e-catalog/1", "accounts": ACCOUNTS, "scenarios": list(scenarios), "contractFixture": {}}


def text(path):
    with open(path) as handle:
        return handle.read()


class Run:
    def __init__(self, code, out, err):
        self.code, self.out, self.err = code, out, err

    @property
    def lines(self):
        return self.out.splitlines()


class Workspace:
    """A temporary repository plus state, fake adapter script and call log. Removed on cleanup."""

    def __init__(self, test, scenarios, script=None, accounts=None):
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-harness-test-"))
        test.addCleanup(shutil.rmtree, self.dir, True)
        self.repo = os.path.join(self.dir, "repo")
        self.state = os.path.join(self.dir, "state")
        self.fake = os.path.join(self.dir, "fake")
        self.wiremock = os.path.join(self.dir, "wiremock")
        for path in (self.repo, self.fake, self.wiremock, os.path.join(self.repo, ".github", "scripts")):
            os.makedirs(path)
        # The real library, not a stand-in: the status context has exactly one definition in the repository.
        shutil.copy(os.path.join(TESTS, "..", "..", "..", ".github", "scripts", "common.sh"),
            os.path.join(self.repo, ".github", "scripts", "common.sh"))
        # common.sh loads the part of the library that lives under the fingerprint.
        os.makedirs(os.path.join(self.repo, "e2e", "scripts", "shared"), exist_ok=True)
        shutil.copy(os.path.join(SHARED, "ci-common.sh"), os.path.join(self.repo, "e2e", "scripts", "shared", "ci-common.sh"))
        shutil.copytree(os.path.join(TESTS, "..", "..", "toolchain"), os.path.join(self.repo, "e2e", "toolchain"))
        shutil.copytree(os.path.join(TESTS, "..", "..", "..", "gradle"), os.path.join(self.repo, "gradle"),
            ignore=shutil.ignore_patterns("wrapper", "*.jar"))
        self.git("init", "-q", "-b", "main")
        self.git("config", "user.name", "Harness Test")
        self.git("config", "user.email", "test@example.invalid")
        self.git("add", "-A")
        self.git("commit", "-q", "-m", "fixture")
        self.git("update-ref", "refs/remotes/origin/main", "HEAD")
        self.git("config", "remote.origin.url", os.path.join(self.dir, "no-such-remote"))
        self.git("config", "remote.origin.fetch", "+refs/heads/*:refs/remotes/origin/*")
        self.git("config", "branch.main.remote", "origin")
        self.git("config", "branch.main.merge", "refs/heads/main")
        self.catalog_path = os.path.join(self.dir, "catalog.json")
        self.call_log = os.path.join(self.fake, "calls.log")
        self.gh_log = os.path.join(self.fake, "gh.log")
        self.gh_fail = os.path.join(self.fake, "gh-fail")
        self.script_path = os.path.join(self.fake, "script.json")
        self.write_catalog(*scenarios, accounts=accounts)
        self.write_script(script or {})
        open(self.call_log, "w").close()
        self.metrics = os.path.join(self.dir, "host-metrics.json")
        with open(self.metrics, "w") as handle:
            json.dump({"load": [0.1, 0.1, 0.1], "ncpu": 10}, handle)
        self.env = self.base_env()

    def git(self, *args):
        subprocess.run(["git"] + list(args), cwd=self.repo, check=True, stdout=subprocess.DEVNULL)

    def write_catalog(self, *scenarios, **kwargs):
        data = catalog(*scenarios)
        if kwargs.get("accounts") is not None:
            data["accounts"] = kwargs["accounts"]
        with open(self.catalog_path, "w") as handle:
            json.dump(data, handle)

    def write_script(self, script):
        with open(self.script_path, "w") as handle:
            json.dump(script, handle)

    def base_env(self):
        env = {k: v for k, v in os.environ.items() if not k.startswith("E2E_")}
        python = shlex.quote(sys.executable)
        env.update({
            "PYTHONDONTWRITEBYTECODE": "1", "E2E_STATE_ROOT": self.state, "E2E_TMP_ROOT": os.path.join(self.dir, "tmp"),
            "E2E_CATALOG_FILE": self.catalog_path, "E2E_FAKE_SCRIPT": self.script_path, "E2E_FAKE_STATE": self.fake,
            "E2E_FAKE_CALL_LOG": self.call_log, "E2E_FAKE_WIREMOCK_DIR": self.wiremock, "E2E_FAKE_OVERHEAD_SECONDS": "1", "E2E_FAKE_HOST_METRICS": self.metrics,
            "E2E_ADAPTER_ANDROID_CMD": "%s %s android" % (python, shlex.quote(FAKE_ADAPTER)),
            "E2E_ADAPTER_IOS_CMD": "%s %s ios" % (python, shlex.quote(FAKE_ADAPTER)),
            "E2E_FINGERPRINT_CMD": "sh -c 'if [ \"$3\" = HEAD ]; then echo \"$E2E_FAKE_FP\"; else echo \"${E2E_FAKE_FP_OTHER:-$E2E_FAKE_FP}\"; fi' _",
            "E2E_FAKE_FP": FP_A,
            "E2E_SCOPE_CMD": "sh -c 'if [ -z \"$E2E_FAKE_SCOPE_EMPTY\" ]; then echo \"$1,${E2E_FAKE_SCOPE_SUITE:-"
                             "local-certification-suite},fixture\"; fi' _",
            "E2E_PUBLISH_GITHUB_STATUS": "0", "E2E_GH_CMD": FAKE_GH, "E2E_FAKE_GH_LOG": self.gh_log,
            "E2E_FAKE_GH_FAIL_FILE": self.gh_fail, "E2E_TEST_ALLOW_SEAMS": "1",
        })
        return env

    def run(self, *argv, **env):
        merged = dict(self.env)
        merged.update(env)
        done = subprocess.run([sys.executable, E2E_PY] + list(argv), cwd=self.repo, env=merged, stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, universal_newlines=True, timeout=300)
        return Run(done.returncode, done.stdout, done.stderr)

    def evidence(self, platform="ios", **env):
        return self.run("run", "--platform", platform, "--mode", "evidence", **env)

    def diagnose(self, platform="ios", *extra, **env):
        return self.run("run", "--platform", platform, "--mode", "diagnose", *extra, **env)

    def calls(self, verb=None, platform=None):
        lines = [line.split(" ") for line in text(self.call_log).splitlines()]
        return [c for c in lines if (verb is None or c[1] == verb) and (platform is None or c[0] == platform)]

    def executed(self, platform="ios"):
        """Scenario ids in the order the runner was invoked."""
        return [c[2] for c in self.calls("run-scenario", platform)]

    def ledger(self, platform="ios", fingerprint=FP_A):
        return self.read(os.path.join(self.state, "ledger", platform, fingerprint, "ledger.json"))

    def run_dirs(self):
        """Run directories of single-platform runs (the `all` parent directory only holds summary.json)."""
        return [d for d in sorted(glob.glob(os.path.join(self.state, "runs", "*")))
            if os.path.exists(os.path.join(d, "manifest.json"))]

    def manifest(self, index=-1):
        return self.read(os.path.join(self.run_dirs()[index], "manifest.json"))

    def manifests(self):
        """{platform: newest manifest}."""
        return {m["platform"]: m for m in (self.read(os.path.join(d, "manifest.json")) for d in self.run_dirs())}

    def set_metrics(self, **values):
        data = {"load": [0.1, 0.1, 0.1], "ncpu": 10}
        data.update(values)
        with open(self.metrics, "w") as handle:
            json.dump(data, handle)

    @staticmethod
    def read(path):
        with open(path) as handle:
            return json.load(handle)
