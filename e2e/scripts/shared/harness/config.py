"""Configuration: layout.env, the E2E_* environment variables and their bounds."""

import os
import re
import shlex
import subprocess
from pathlib import Path

PLATFORMS = ("android", "ios")
SUITE_ID = "local-certification-suite"
MODES = ("evidence", "diagnose")

# The only env-check ids that can refuse a run, hence the only ones E2E_ENV_OVERRIDE accepts.
REFUSABLE_CHECKS = ("cpu", "disk")

# Time the runner needs besides the scenario's own timeout (build of the test
# process, xcodebuild start-up), added to the kill deadline.
RUNNER_OVERHEAD_SECONDS = {"android": 30, "ios": 90}

# Deadlines of the adapter verbs other than run-scenario, in seconds. Fixed in F16 from measurements on the real
# emulator and simulator (see the F16 report); a verb that is far under its deadline is on purpose: the deadline
# only has to catch a hang, and the host load multiplies every time here.
VERB_TIMEOUTS = {
    "toolchain": 30,        # measured 0.3-1.0 s
    "ensure-device": 420,   # Android cold boot gate 240 s + settings; iOS first creation + erased boot measured 67 s
    "build": 1800,          # iOS cold derived data 92 s, warm 7-11 s; Android all cached 4 s; a clean Gradle build is not measured
    "install": 120,         # measured 0.6-3 s
    "enumerate": 120,       # measured 6-7 s
    "health": 180,          # the Android probe may wait up to 120 s for the emulator load to drop; measured 0.2 s idle
    "reset-app": 60,        # measured 0.5 s Android, 1.9 s iOS
    "crash-probe": 30,      # measured 0.2 s Android, 1 s iOS
    "collect-failure": 60,  # measured 3-4 s
    "recover": 600,         # not measured: device.sh recover refuses on the Android locale mismatch
    "stop-device": 120,     # measured 1.2 s Android, 3.5 s iOS
}


class UsageError(Exception):
    """Exit 2: bad usage or a precondition that does not hold."""


class EnvironmentRefused(Exception):
    """Exit 3: the environment is refused or cannot be recovered."""


def parse_layout(path):
    values = {}
    for line in Path(path).read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        key, _, value = line.partition("=")
        values[key.strip()] = value.strip()
    return values


def find_repo_root(start=None):
    here = Path(start or os.getcwd())
    result = subprocess.run(
        ["git", "rev-parse", "--show-toplevel"], cwd=str(here),
        stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, universal_newlines=True,
    )
    if result.returncode == 0 and result.stdout.strip():
        return Path(result.stdout.strip()).resolve()
    return Path(__file__).resolve().parents[4]


def _int(env, name, default, low, high):
    raw = env.get(name, "")
    if raw == "":
        return default
    if not re.fullmatch(r"-?\d+", raw) or not low <= int(raw) <= high:
        raise UsageError("%s=%s is out of range (%d..%d)" % (name, raw, low, high))
    return int(raw)


def _choice(env, name, default, choices):
    value = env.get(name, "") or default
    if value not in choices:
        raise UsageError("%s=%s is not one of: %s" % (name, value, ", ".join(choices)))
    return value


class Config:
    def __init__(self, root, env=None):
        env = dict(os.environ if env is None else env)
        self.root = Path(root)
        self.layout = parse_layout(self.root_script_dir() / "layout.env")
        self.max_retries = _int(env, "E2E_MAX_RETRIES", 1, 0, 2)
        self.budget_minutes = _int(env, "E2E_BUDGET_MINUTES", 120, 10, 240)
        self.parallel = _choice(env, "E2E_PARALLEL", "auto", ("auto", "never", "always"))
        self.publish_setting = _choice(env, "E2E_PUBLISH_GITHUB_STATUS", "auto", ("auto", "0", "1"))
        self.skip = {
            "android": _choice(env, "E2E_SKIP_ANDROID", "0", ("0", "1")) == "1",
            "ios": _choice(env, "E2E_SKIP_IOS", "0", ("0", "1")) == "1",
        }
        self.strict_ios = _choice(env, "E2E_STRICT_IOS", "1", ("0", "1")) == "1"
        self.ports = {
            "android": _int(env, "E2E_ANDROID_WIREMOCK_PORT", 18626, 1024, 65535),
            "ios": _int(env, "E2E_IOS_WIREMOCK_PORT", 18627, 1024, 65535),
        }
        self.tunnel = _choice(env, "E2E_ANDROID_TUNNEL", "host-alias", ("host-alias", "reverse"))
        self.delay_profile = _choice(env, "E2E_WIREMOCK_DELAY_PROFILE", "fast", ("fast", "legacy"))
        self.env_override = [item for item in env.get("E2E_ENV_OVERRIDE", "").split(",") if item]
        unknown = [item for item in self.env_override if item not in REFUSABLE_CHECKS]
        if unknown:
            raise UsageError("E2E_ENV_OVERRIDE accepts only %s; got %s" % (", ".join(REFUSABLE_CHECKS), ", ".join(unknown)))
        self.scenario_filter =[item for item in env.get("E2E_SCENARIOS", "").split(",") if item]
        self.artifacts_max_gb = _int(env, "E2E_ARTIFACTS_MAX_GB", 5, 1, 1000)
        tmp_default = os.path.join(env.get("TMPDIR") or "/tmp", "tuindice-e2e")
        self.tmp_root = Path(os.path.realpath(env.get("E2E_TMP_ROOT") or tmp_default))
        state = Path(env.get("E2E_STATE_ROOT") or "build/e2e")
        self.state_root = state if state.is_absolute() else self.root / state
        self.env = env

    def root_script_dir(self):
        return Path(__file__).resolve().parent.parent

    def seam(self, name):
        return self.env.get(name) or None

    def layout_path(self, key):
        return self.root / self.layout[key]

    @property
    def catalog_path(self):
        return Path(self.seam("E2E_CATALOG_FILE") or self.layout_path("E2E_CATALOG_JSON"))

    @property
    def budget_seconds(self):
        return self.budget_minutes * 60

    @property
    def attempt_cap(self):
        return 1 + self.max_retries

    def overhead_seconds(self, platform):
        fake = self.seam("E2E_FAKE_OVERHEAD_SECONDS")
        return float(fake) if fake is not None else RUNNER_OVERHEAD_SECONDS[platform]

    def publish_required(self, mode):
        if mode != "evidence":
            return False
        return self.publish_setting != "0"

    def adapter_command(self, platform):
        fake = self.seam("E2E_ADAPTER_%s_CMD" % platform.upper())
        if fake:
            return shlex.split(fake)
        return ["bash", str(self.root / self.layout["E2E_ADAPTER_ROOT"] / platform / "adapter.sh")]

    def platforms_for(self, selector):
        if selector != "all":
            return [selector]
        chosen = [p for p in PLATFORMS if not self.skip[p]]
        if not chosen:
            raise UsageError("E2E_SKIP_ANDROID and E2E_SKIP_IOS skip every platform")
        return chosen
