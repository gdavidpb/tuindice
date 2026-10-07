"""Toolchain locks: e2e/toolchain/<platform>.lock against what the adapter's `toolchain` verb reports."""

import re

from .config import EnvironmentRefused, UsageError, parse_layout

LOCK_KEYS = {
    "ios": ("XCODE_VERSION", "XCODE_BUILD", "IOS_RUNTIME_ID", "IOS_RUNTIME_BUILD", "IOS_DEVICE_TYPE_ID",
        "IOS_SIMULATOR_NAME", "IOS_LANGUAGE", "IOS_LOCALE"),
    "android": ("ANDROID_AVD_NAME", "ANDROID_SYSTEM_IMAGE_DIR", "ANDROID_SYSTEM_IMAGE_REVISION", "ANDROID_API_LEVEL",
        "ANDROID_DEVICE_PROFILE", "ANDROID_LOCALE", "ANDROID_EMULATOR_MEMORY_MB", "ANDROID_EMULATOR_CORES",
        "ANDROID_EMULATOR_HEADLESS"),
}
# Lock keys nothing ever checks: a boot flag only matters when the harness starts the emulator itself, and nothing reads it back
# from one that is already running. The manifest and the log say so instead of counting them among the verified.
NOT_VERIFIED = ("ANDROID_EMULATOR_HEADLESS",)
# Versions recorded in the manifest; they are covered by gradle/libs.versions.toml, which is in the fingerprint.
LIB_KEYS = ("kotlin", "android-gradle-plugin", "uiautomator", "test-runner", "compose-mpp")


def lock_path(cfg, platform):
    return cfg.root / cfg.layout["E2E_TOOLCHAIN_DIR"] / ("%s.lock" % platform)


def read_lock(cfg, platform):
    """The lock as a dict. A missing, unknown or absent key is a usage error (exit 2)."""
    path = lock_path(cfg, platform)
    if not path.exists():
        raise UsageError("the toolchain lock %s does not exist" % path)
    values = parse_layout(path)
    missing = [k for k in LOCK_KEYS[platform] if not values.get(k)]
    unknown = [k for k in values if k not in LOCK_KEYS[platform]]
    if missing or unknown:
        raise UsageError("%s: missing keys [%s], unknown keys [%s]" % (path.name, ", ".join(missing), ", ".join(unknown)))
    return values


def compare(lock, actual):
    """(differences, unverified). A key the adapter reports as null is verified later, by `ensure-device`;
    a key it does not report at all is a difference."""
    differences, unverified = [], []
    for key, expected in lock.items():
        if key not in actual:
            differences.append((key, expected, "<not reported>"))
        elif actual[key] is None:
            unverified.append(key)
        elif str(actual[key]) != expected:
            differences.append((key, expected, str(actual[key])))
    return differences, unverified


def libs(cfg):
    versions = {}
    in_versions = False
    for line in open(str(cfg.root / "gradle" / "libs.versions.toml"), errors="replace"):
        if line.startswith("["):
            in_versions = line.strip() == "[versions]"
        match = re.match(r'([A-Za-z0-9_-]+)\s*=\s*"([^"]*)"', line)
        if in_versions and match and match.group(1) in LIB_KEYS:
            versions[match.group(1)] = match.group(2)
    return versions


def check(run):
    """Hook of the runner: evidence refuses on a difference (exit 3); diagnose and dry-run only report."""
    lock = read_lock(run.cfg, run.platform)
    result = run.adapter.call("toolchain")
    if not result.ok or not result.json:
        raise EnvironmentRefused("the toolchain verb failed (exit %s): %s" % (result.returncode, result.stderr.strip()[-300:]))
    differences, unverified = compare(lock, result.json)
    never = [key for key in unverified if key in NOT_VERIFIED]
    unverified = [key for key in unverified if key not in NOT_VERIFIED]
    informational = {k: v for k, v in result.json.items() if k not in lock}
    run.manifest.update(toolchain={"lockFile": str(lock_path(run.cfg, run.platform).relative_to(run.cfg.root)),
        "lockMatches": not differences, "actual": {k: result.json.get(k) for k in lock}, "unverified": unverified,
        "neverVerified": never,
        "informational": informational, "libs": libs(run.cfg)})
    if not differences:
        run.log.say("TOOLCHAIN matches %s (%d keys; %d more verified when the device starts%s)"
            % (lock_path(run.cfg, run.platform).name, len(lock) - len(unverified) - len(never), len(unverified),
                "; not verified at all: %s" % ", ".join(never) if never else ""))
        return
    for key, expected, actual in differences:
        run.log.say("TOOLCHAIN DIFFERS %s: lock %s, actual %s" % (key, expected, actual))
    if run.evidence and not run.opts.dry_run:
        raise EnvironmentRefused("the toolchain differs from %s in %d keys; evidence needs the pinned toolchain "
            "(update the lock in a deliberate commit or restore the tools)" % (lock_path(run.cfg, run.platform).name, len(differences)))
