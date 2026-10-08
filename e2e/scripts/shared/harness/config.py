"""Configuration: layout.env, the E2E_* environment variables and their bounds."""

import os
import re
import shlex
import subprocess
from pathlib import Path

PLATFORMS = ("android", "ios")
EXIT_HARNESS_ERROR = 70  # a defect of the harness itself, not a verdict about the scenarios

# The only env-check ids that can refuse a run, hence the only ones E2E_ENV_OVERRIDE accepts.
REFUSABLE_CHECKS = ("cpu", "disk")

# A simulator that degrades is recovered again only after this many green scenarios since the previous recovery; sooner than that
# it is not a usable simulator and the run is exit 3.
DEGRADATION_MIN_GREEN = 30

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
    "driver-contract": 420, # measured 19 s Android, 26 s iOS by hand; the app is reset first and the Android probes ride along
    "health": 180,          # the Android probe may wait up to 120 s for the emulator load to drop; measured 0.2 s idle
    "reset-app": 60,        # measured 0.5 s Android, 1.9 s iOS
    "crash-probe": 45,      # measured 0.2 s Android, 1 s iOS; a failed iOS attempt may wait 15 s for the crash report
    "collect-failure": 60,  # measured 3-4 s
    "recover": 180,         # measured 53.5 s on the Android emulator (reboot, gate, settings), 58 s for an iOS boot
    "stop-device": 120,     # measured 1.2 s Android, 3.5 s iOS
}


# Test seams: variables that replace a piece of the harness (an adapter, the scope, the catalog, the host measures). They
# exist so that the harness can be tested without devices. Evidence refuses to run with any of them set (the ledger records
# the ones a run used and `publish` refuses such a ledger), because each one changes what is measured or certified without
# leaving a trace in the status. The one key below is the harness tests' explicit permission to run and publish through
# fakes (a fake `gh`); nothing else sets it.
ALLOW_SEAMS_KEY = "E2E_TEST_ALLOW_SEAMS"
SEAM_NAMES = frozenset(("E2E_CATALOG_FILE", "E2E_SCOPE_FILE", "DETECT_CHANGED_APP_CHANGED_FILES_FILE", "E2E_FINGERPRINT_REPO_ROOT"))


def is_seam(name):
    return name in SEAM_NAMES or name.startswith("E2E_FAKE_") or (name.startswith("E2E_") and name.endswith("_CMD"))


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


LAYOUT = Path(__file__).resolve().parent.parent / "layout.env"


def shared_function(root, function, *args):
    """(exit code, lines) of a function of the shared shell library (.github/scripts/common.sh, which loads
    e2e/scripts/shared/ci-common.sh): the status context, the base ref, the trusted creators and the suite id each have
    one definition, in ci-common.sh."""
    source = Path(root) / parse_layout(LAYOUT)["E2E_STATUS_CONTEXT_SOURCE"]
    if not source.exists():
        raise UsageError("%s does not exist" % source)
    result = subprocess.run(["bash", "-c", 'source "$1"; shift; "$@"', "_", str(source), function] + [str(a) for a in args],
        cwd=str(root), stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, universal_newlines=True)
    return result.returncode, result.stdout.split("\n") if result.stdout else []


def _suite_id():
    """E2E_SUITE_ID of the shared library: the harness has no literal of its own."""
    code, lines = shared_function(Path(__file__).resolve().parents[4], "eval", 'printf "%s" "$E2E_SUITE_ID"')
    if code != 0 or not lines or not lines[0]:
        raise UsageError("E2E_SUITE_ID is not defined in the shared shell library")
    return lines[0]


SUITE_ID = _suite_id()


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
        self.ports = {
            "android": _int(env, "E2E_ANDROID_WIREMOCK_PORT", 18626, 1024, 65535),
            "ios": _int(env, "E2E_IOS_WIREMOCK_PORT", 18627, 1024, 65535),
        }
        _choice(env, "E2E_ANDROID_TUNNEL", "host-alias", ("host-alias", "reverse"))  # read by the Android scripts; only checked here
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

    def run_overrides(self):
        """The knobs a run turned away from their defaults; the status description counts them."""
        found = ["env-check:%s" % item for item in self.env_override]
        found += ["parallel:%s" % self.parallel] if self.parallel != "auto" else []
        found += ["retries:%d" % self.max_retries] if self.max_retries != 1 else []
        return found

    def seams(self):
        """The test seams set in the environment (sorted names), the permission key included when present."""
        return sorted(name for name in self.env if (is_seam(name) or name == ALLOW_SEAMS_KEY) and self.env[name] != "")

    def require_no_seams(self, what, recorded=()):
        """Evidence refuses every seam, and so does publishing it (a status on GitHub): the ones set now and the ones the
        ledger recorded for its runs. Only the harness tests' key allows them."""
        found = sorted((set(self.seams()) | set(recorded)) - {ALLOW_SEAMS_KEY})
        if found and self.env.get(ALLOW_SEAMS_KEY) != "1":
            raise UsageError("%s needs a real environment: unset the test seams %s" % (what, ", ".join(found)))

    def require_clean_ledger(self, directory, recorded):
        """Evidence starts only on a ledger whose runs used no test seam: its greens could come from a fake adapter. Only the
        harness tests' key allows it. The environment has none to unset: the ledger is the thing to set aside."""
        found = sorted(set(recorded) - {ALLOW_SEAMS_KEY})
        if found and self.env.get(ALLOW_SEAMS_KEY) != "1":
            raise UsageError("the ledger %s recorded runs with test seams (%s), so its greens may come from a fake; set it aside "
                "(move that directory out of the state root) and run the evidence again" % (directory, ", ".join(found)))

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
