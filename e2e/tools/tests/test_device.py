"""device.sh of both platforms against fake adb, fake emulator and fake xcrun (plan F15)."""

import json
import os
import shutil
import subprocess
import tempfile
import unittest

import support
from harness.config import parse_layout

SCRIPTS = os.path.join(support.SHARED, "..")
FAKE_BIN = os.path.join(support.TESTS, "fake_bin")
LOCKS = os.path.join(support.TESTS, "..", "..", "toolchain")


class Sandbox:
    """A device.sh invocation environment: PATH starts with the fakes, every state path is a temporary directory."""

    def __init__(self, test, platform):
        self.platform = platform
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-device-test-"))
        test.addCleanup(shutil.rmtree, self.dir, True)
        self.lock = parse_layout(os.path.join(LOCKS, "%s.lock" % platform))
        self.script = os.path.join(SCRIPTS, platform, "device.sh")
        self.adb = os.path.join(self.dir, "adb")
        self.xcrun = os.path.join(self.dir, "xcrun")
        self.env = dict(os.environ, HOME=self.dir, PATH=FAKE_BIN + os.pathsep + os.environ["PATH"],
            FAKE_ADB_DIR=self.adb, FAKE_XCRUN_DIR=self.xcrun, E2E_TMP_ROOT=os.path.join(self.dir, "tmp"),
            E2E_FAKE_GATE_SECONDS="3", E2E_FAKE_GATE_POLL_SECONDS="0.1", E2E_FAKE_HEALTH_LOAD_WAIT_SECONDS="1",
            E2E_FAKE_SIM_DEVICES_DIR=os.path.join(self.dir, "devices"), E2E_FAKE_HOST_DOMAIN=os.path.join(self.dir, "host-prefs"),
            ANDROID_HOME=os.path.join(self.dir, "sdk"), ANDROID_AVD_HOME=os.path.join(self.dir, "avd"))
        for key in ("ANDROID_SDK_ROOT", "E2E_ANDROID_TUNNEL", "E2E_ANDROID_WIREMOCK_PORT"):
            self.env.pop(key, None)
        os.makedirs(self.adb)
        os.makedirs(self.xcrun)
        if platform == "android":
            self.build_sdk()

    def build_sdk(self, image_dir=None, revision=None):
        lock = self.lock
        image = os.path.join(self.dir, "sdk", lock["ANDROID_SYSTEM_IMAGE_DIR"])
        os.makedirs(image, exist_ok=True)
        with open(os.path.join(image, "source.properties"), "w") as handle:
            handle.write("Pkg.Revision=%s\nAndroidVersion.ApiLevel=37.1\n" % (revision or lock["ANDROID_SYSTEM_IMAGE_REVISION"]))
        os.makedirs(os.path.join(self.dir, "sdk", "emulator"), exist_ok=True)
        shutil.copy(os.path.join(FAKE_BIN, "emulator"), os.path.join(self.dir, "sdk", "emulator", "emulator"))
        avd = os.path.join(self.dir, "avd", lock["ANDROID_AVD_NAME"] + ".avd")
        os.makedirs(avd, exist_ok=True)
        with open(os.path.join(self.dir, "avd", lock["ANDROID_AVD_NAME"] + ".ini"), "w") as handle:
            handle.write("path=%s\n" % avd)
        with open(os.path.join(avd, "config.ini"), "w") as handle:
            handle.write("image.sysdir.1=%s/\nhw.device.name=%s\n" % (image_dir or lock["ANDROID_SYSTEM_IMAGE_DIR"], lock["ANDROID_DEVICE_PROFILE"]))

    def run(self, verb, **env):
        done = subprocess.run(["bash", self.script, verb], env=dict(self.env, **env), stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, universal_newlines=True, timeout=60)
        done.json = json.loads(done.stdout.strip().splitlines()[-1]) if done.returncode == 0 and done.stdout.strip() else {}
        return done

    def write(self, name, text, base=None):
        path = os.path.join(base or self.adb, name)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w") as handle:
            handle.write(text)

    def calls(self, base=None):
        path = os.path.join(base or self.adb, "calls.log")
        return support.text(path).splitlines() if os.path.exists(path) else []

    def online(self):
        """An emulator that is up and past the readiness gate, as a previous `ensure` leaves it."""
        self.write("online", "")
        self.write("settings/global.hide_error_dialogs", "1\n")


class AndroidDeviceTests(unittest.TestCase):
    def test_ensure_boots_with_the_lock_parameters_and_reads_the_settings_back(self):
        box = Sandbox(self, "android")
        done = box.run("ensure")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(support.text(os.path.join(box.adb, "emulator.args")).strip(),
            "-avd Pixel_10_Pro_XL -no-snapshot -no-boot-anim -no-audio -memory 16384 -cores 8 -no-window")
        self.assertTrue(done.json["bootedByHarness"])
        self.assertEqual(done.json["id"], "emulator-5554")
        self.assertEqual(done.json["settings"]["global.animator_duration_scale"], "0")
        self.assertEqual(done.json["settings"]["secure.autofill_service"], "null")
        self.assertIn("shell settings put global hide_error_dialogs 1", "\n".join(box.calls()))

    def test_ensure_applies_and_reads_back_every_setting_the_driver_used_to_apply_itself(self):
        # One list lives in device.sh; the scenario runner no longer changes device settings.
        box = Sandbox(self, "android")
        done = box.run("ensure")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(done.json["settings"]["global.hide_error_dialogs"], "1")
        self.assertEqual(done.json["settings"]["secure.stylus_handwriting_enabled"], "0")
        calls = "\n".join(box.calls())
        self.assertIn("shell settings put secure stylus_handwriting_enabled 0", calls)
        self.assertIn("shell settings get secure stylus_handwriting_enabled", calls)

    def test_a_du_that_warns_while_it_walks_the_data_directory_does_not_fail_ensure(self):
        # du exits 1 when a file vanishes under it and still prints the total; the device is fine and ensure has done everything.
        box = Sandbox(self, "android")
        done = box.run("ensure", FAKE_DU_KB="1048576", FAKE_DU_EXIT="1")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(done.json["dataDirGb"], 1.0)

    def test_a_du_that_prints_no_total_is_still_an_environment_failure(self):
        box = Sandbox(self, "android")
        done = box.run("ensure", FAKE_DU_NONE="1", FAKE_DU_EXIT="1")
        self.assertEqual(done.returncode, 3)
        self.assertIn("gave no total", done.stderr)

    def test_headless_zero_starts_the_window(self):
        box = Sandbox(self, "android")
        lock = os.path.join(box.dir, "android.lock")
        with open(os.path.join(LOCKS, "android.lock")) as source, open(lock, "w") as target:
            target.write(source.read().replace("ANDROID_EMULATOR_HEADLESS=1", "ANDROID_EMULATOR_HEADLESS=0"))
        self.assertEqual(box.run("ensure", E2E_FAKE_LOCK_FILE=lock).returncode, 0)
        self.assertNotIn("-no-window", support.text(os.path.join(box.adb, "emulator.args")))

    def test_a_booted_device_is_not_restarted(self):
        box = Sandbox(self, "android")
        box.online()
        done = box.run("ensure")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertFalse(done.json["bootedByHarness"])
        self.assertFalse(os.path.exists(os.path.join(box.adb, "emulator.args")), "an online emulator must not be booted again")
        self.assertFalse([c for c in box.calls() if "reboot" in c or "emu kill" in c])

    def test_a_setting_that_reads_back_a_different_value_exits_3(self):
        box = Sandbox(self, "android")
        box.online()
        box.write("settings/global.animator_duration_scale", "1.0\n")
        box.write("ignore", "global animator_duration_scale\n")
        done = box.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertIn("global animator_duration_scale reads back '1.0', expected '0'", done.stderr)

    def test_a_gate_that_never_opens_exits_3(self):
        box = Sandbox(self, "android")
        box.write("boot_completed", "0\n")
        done = box.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertIn("did not finish booting within 3s", done.stderr)

    def test_the_gate_opens_when_the_boot_animation_service_does_not_exist(self):
        # Measured on the real emulator: with -no-boot-anim the property init.svc.bootanim is never set.
        box = Sandbox(self, "android")
        box.write("props/bootanim", "")
        done = box.run("ensure")
        self.assertEqual(done.returncode, 0, done.stderr)

    def test_a_running_boot_animation_keeps_the_gate_closed(self):
        box = Sandbox(self, "android")
        box.write("props/bootanim", "running\n")
        done = box.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertIn("did not finish booting within 3s", done.stderr)

    def test_the_gate_waits_for_the_device_load_and_an_anr_window_blocks_it(self):
        box = Sandbox(self, "android")
        box.online()
        box.write("loadavg.seq", "9.50\n8.00\n2.00\n")
        self.assertEqual(box.run("ensure").returncode, 0)
        busy = Sandbox(self, "android")
        busy.online()
        busy.write("loadavg.seq", "12.00\n")
        done = busy.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertIn("device load 12.00", done.stderr)
        anr = Sandbox(self, "android")
        anr.online()
        anr.write("anr", "")
        self.assertEqual(anr.run("ensure").returncode, 3)

    def test_the_wrong_avd_image_or_a_missing_avd_exit_3(self):
        wrong = Sandbox(self, "android")
        wrong.build_sdk(image_dir="system-images/android-36/google_apis/arm64-v8a")
        self.assertIn("uses the image 'system-images/android-36", wrong.run("ensure").stderr)
        stale = Sandbox(self, "android")
        stale.build_sdk(revision="8")
        done = stale.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertIn("revision '8', the lock pins 9", done.stderr)
        missing = Sandbox(self, "android")
        os.remove(os.path.join(missing.dir, "avd", "Pixel_10_Pro_XL.ini"))
        done = missing.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertIn("avdmanager create avd -n Pixel_10_Pro_XL", done.stderr)
        self.assertFalse(os.path.exists(os.path.join(missing.adb, "emulator.args")), "the harness never creates or boots a missing AVD")

    def test_the_shape_of_a_running_emulator_must_match_the_lock(self):
        for prop, value, message in (("nproc", "4", "has 4 cores"), ("meminfo_kb", "2000000", "reports 2000000 kB"),
                ("locale", "es-VE", "locale is 'es-VE'"), ("sdk", "36", "API level is '36'")):
            box = Sandbox(self, "android")
            box.online()
            box.write("props/" + prop, value + "\n")
            done = box.run("ensure")
            self.assertEqual(done.returncode, 3, prop)
            self.assertIn(message, done.stderr)

    def test_health_waits_for_a_busy_device_and_records_the_load(self):
        box = Sandbox(self, "android")
        box.online()
        box.write("loadavg.seq", "9.00\n7.00\n3.40\n")
        done = box.run("health")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(done.json["deviceLoad1"], 3.4)
        self.assertGreaterEqual(done.json["loadWaitSeconds"], 0)

    def test_health_fails_when_the_load_never_drops(self):
        box = Sandbox(self, "android")
        box.online()
        box.write("loadavg.seq", "12.30\n")
        done = box.run("health")
        self.assertEqual(done.returncode, 3)
        self.assertIn("device load 12.30 did not drop below 6.0 within 1s", done.stderr)

    def test_health_checks_boot_error_dialogs_and_anr(self):
        for setup, message in (
                (lambda b: b.write("boot_completed", "0\n"), "sys.boot_completed is not 1"),
                (lambda b: b.write("settings/global.hide_error_dialogs", "0\n"), "hide_error_dialogs is not 1"),
                (lambda b: b.write("anr", ""), "Application Not Responding")):
            box = Sandbox(self, "android")
            box.online()
            setup(box)
            done = box.run("health")
            self.assertEqual(done.returncode, 3, message)
            self.assertIn(message, done.stderr)

    def test_recover_reboots_the_pinned_device_and_reestablishes_the_reverse_tunnel(self):
        box = Sandbox(self, "android")
        box.online()
        done = box.run("recover", E2E_ANDROID_TUNNEL="reverse", E2E_ANDROID_WIREMOCK_PORT="18626")
        self.assertEqual(done.returncode, 0, done.stderr)
        calls = box.calls()
        self.assertIn("-s emulator-5554 reboot", calls)
        self.assertLess(calls.index("-s emulator-5554 reboot"), calls.index("-s emulator-5554 reverse tcp:18626 tcp:18626"))
        self.assertEqual(box.run("health", E2E_ANDROID_TUNNEL="reverse").returncode, 0)

    def test_stop_kills_only_the_pinned_emulator(self):
        box = Sandbox(self, "android")
        box.online()
        self.assertEqual(box.run("stop").returncode, 0)
        self.assertIn("-s emulator-5554 emu kill", box.calls())
        self.assertFalse(os.path.exists(os.path.join(box.adb, "online")))

    def test_toolchain_reads_files_only_and_reports_device_keys_as_null(self):
        box = Sandbox(self, "android")
        done = box.run("toolchain")
        self.assertEqual(done.returncode, 0, done.stderr)
        for key in ("ANDROID_AVD_NAME", "ANDROID_SYSTEM_IMAGE_DIR", "ANDROID_SYSTEM_IMAGE_REVISION", "ANDROID_DEVICE_PROFILE"):
            self.assertEqual(done.json[key], box.lock[key], key)
        self.assertEqual(done.json["ANDROID_API_LEVEL"], "37")
        self.assertIsNone(done.json["ANDROID_LOCALE"])
        self.assertFalse([c for c in box.calls() if "-s" in c or "devices" in c], "toolchain must not query a device")


class IosDeviceTests(unittest.TestCase):
    def ensure(self, state=None, **env):
        box = Sandbox(self, "ios")
        if state:
            box.write("sim", "FAKE-0000-0000-0000-000000000001 %s\n" % state, box.xcrun)
        return box, box.run("ensure", **env)

    @support.requires_macos("ios/device.sh writes the simulator settings through the host `defaults` command")
    def test_a_missing_simulator_is_created_from_the_lock_erased_booted_and_configured(self):
        box, done = self.ensure()
        self.assertEqual(done.returncode, 0, done.stderr)
        calls = box.calls(box.xcrun)
        self.assertIn("simctl create TuIndice-E2E com.apple.CoreSimulator.SimDeviceType.iPhone-18-Pro "
            "com.apple.CoreSimulator.SimRuntime.iOS-27-0", calls)
        self.assertLess(calls.index("simctl erase FAKE-0000-0000-0000-000000000001"),
            calls.index("simctl boot FAKE-0000-0000-0000-000000000001"))
        self.assertTrue(done.json["created"] and done.json["bootedByHarness"])
        self.assertEqual(done.json["model"], "iPhone 18 Pro")
        self.assertEqual(done.json["settings"]["-g.KeyboardAutocorrection"], "0")
        self.assertEqual(done.json["settings"]["-g.AppleLanguages"], "es")
        self.assertEqual(done.json["settings"]["ConnectHardwareKeyboard"], "false")

    @support.requires_macos("ios/device.sh writes the simulator settings through the host `defaults` command")
    def test_the_active_keyboards_are_pinned_from_the_lock_and_read_back(self):
        box, done = self.ensure()
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(box.lock["IOS_KEYBOARDS"], "es_ES@sw=QWERTY;hw=Automatic", "the layout that goes with IOS_LANGUAGE=es")
        self.assertEqual(done.json["settings"]["-g.AppleKeyboards"], box.lock["IOS_KEYBOARDS"])
        self.assertTrue(any(c.startswith("simctl spawn FAKE-0000-0000-0000-000000000001 defaults write -g AppleKeyboards -array")
            for c in box.calls(box.xcrun)))

    def test_active_keyboards_that_do_not_read_back_exit_3(self):
        box = Sandbox(self, "ios")
        box.write("ignore", "-g AppleKeyboards\n", box.xcrun)
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        done = box.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertIn("-g AppleKeyboards reads back ''", done.stderr)
        self.assertIn("es_ES@sw=QWERTY;hw=Automatic", done.stderr)

    @support.requires_macos("ios/device.sh writes the simulator settings through the host `defaults` command")
    def test_a_shutdown_simulator_is_erased_and_booted_but_a_booted_one_is_reused(self):
        box, done = self.ensure("Shutdown")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertFalse(done.json["created"])
        self.assertTrue(done.json["bootedByHarness"])
        box, done = self.ensure("Booted")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertFalse(done.json["bootedByHarness"])
        self.assertEqual([c for c in box.calls(box.xcrun) if c.split()[1] in ("boot", "erase", "shutdown")], [],
            "a booted simulator must not be rebooted")

    @support.requires_macos("ios/device.sh writes the simulator settings through the host `defaults` command")
    def test_a_simulator_above_the_data_limit_is_erased_and_booted(self):
        box = Sandbox(self, "ios")
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        box.write("FAKE-0000-0000-0000-000000000001/data/big", "x" * 4096, os.path.join(box.dir, "devices"))
        done = box.run("ensure", E2E_FAKE_IOS_DATA_LIMIT_GB="0")
        self.assertEqual(done.returncode, 0, done.stderr)
        calls = box.calls(box.xcrun)
        self.assertLess(calls.index("simctl shutdown FAKE-0000-0000-0000-000000000001"),
            calls.index("simctl erase FAKE-0000-0000-0000-000000000001"))
        self.assertTrue(done.json["bootedByHarness"])

    @support.requires_macos("ios/device.sh writes the simulator settings through the host `defaults` command")
    def test_a_du_that_warns_while_it_walks_the_simulator_data_does_not_fail_ensure(self):
        box = Sandbox(self, "ios")
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        box.write("FAKE-0000-0000-0000-000000000001/data/x", "x", os.path.join(box.dir, "devices"))
        done = box.run("ensure", FAKE_DU_KB="1048576", FAKE_DU_EXIT="1")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(done.json["dataDirGb"], 1.0)

    def test_a_preference_that_does_not_read_back_exits_3(self):
        box = Sandbox(self, "ios")
        box.write("ignore", "-g KeyboardPrediction\n", box.xcrun)
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        done = box.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertIn("-g KeyboardPrediction reads back ''", done.stderr)

    @support.requires_macos("ios/device.sh writes the simulator settings through the host `defaults` command")
    def test_a_booted_simulator_that_does_not_answer_gets_one_recovery_inside_ensure(self):
        box = Sandbox(self, "ios")
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        box.write("fail-first-write", "", box.xcrun)  # the first `defaults write` fails: Could not write domain Apple Global Domain
        done = box.run("ensure")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertTrue(done.json["recoveredAtEnsure"])
        self.assertFalse(done.json["bootedByHarness"], "it was booted before the run; the recovery is recorded apart")
        calls = box.calls(box.xcrun)
        self.assertEqual(sum(1 for c in calls if c.startswith("simctl shutdown")), 1)
        self.assertLess(calls.index("simctl shutdown FAKE-0000-0000-0000-000000000001"),
            calls.index("simctl boot FAKE-0000-0000-0000-000000000001"))
        self.assertIn("could not be applied", done.stderr)
        self.assertEqual(done.json["settings"]["-g.AppleLanguages"], "es")

    def test_a_simulator_that_refuses_its_settings_after_the_recovery_exits_3_with_the_reason(self):
        box = Sandbox(self, "ios")
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        box.write("ignore", "-g KeyboardPrediction\n", box.xcrun)
        done = box.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertEqual(sum(1 for c in box.calls(box.xcrun) if c.startswith("simctl shutdown")), 1, "one recovery, not a loop")
        self.assertIn("refused its settings again after one recovery", done.stderr)
        self.assertIn("-g KeyboardPrediction reads back ''", done.stderr)

    @support.requires_macos("ios/device.sh writes the simulator settings through the host `defaults` command")
    def test_a_healthy_simulator_is_not_recovered(self):
        box, done = self.ensure("Booted")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertFalse(done.json["recoveredAtEnsure"])

    def test_an_existing_simulator_of_another_device_type_exits_3(self):
        box = Sandbox(self, "ios")
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        box.write("type", "com.apple.CoreSimulator.SimDeviceType.iPhone-17\n", box.xcrun)
        done = box.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertIn("is of type com.apple.CoreSimulator.SimDeviceType.iPhone-17, the lock pins", done.stderr)
        self.assertEqual([c for c in box.calls(box.xcrun) if c.split()[1] in ("boot", "erase", "shutdown", "create")], [])

    @support.requires_macos("ios/device.sh writes the simulator settings through the host `defaults` command")
    def test_a_simctl_that_does_not_report_the_device_type_says_it_could_not_verify_it(self):
        box = Sandbox(self, "ios")
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        box.write("type", "omit\n", box.xcrun)
        done = box.run("ensure")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertIn("did not report the device type", done.stderr)
        self.assertIs(done.json["deviceTypeVerified"], False)
        box.write("type", "com.apple.CoreSimulator.SimDeviceType.iPhone-18-Pro\n", box.xcrun)
        again = box.run("ensure")
        self.assertIs(again.json["deviceTypeVerified"], True)
        self.assertNotIn("did not report the device type", again.stderr)

    def test_a_simctl_that_fails_is_not_an_absent_simulator(self):
        box = Sandbox(self, "ios")
        box.write("list-fails", "", box.xcrun)
        for verb in ("ensure", "health", "toolchain"):
            done = box.run(verb)
            self.assertEqual(done.returncode, 3, verb)
            self.assertIn("simctl list devices failed", done.stderr)
        self.assertFalse([c for c in box.calls(box.xcrun) if "create" in c], "a second TuIndice-E2E must never be created")

    def test_a_missing_runtime_exits_3_before_creating_anything(self):
        box = Sandbox(self, "ios")
        lock = os.path.join(box.dir, "ios.lock")
        with open(os.path.join(LOCKS, "ios.lock")) as source, open(lock, "w") as target:
            target.write(source.read().replace("iOS-27-0", "iOS-99-0"))
        done = box.run("ensure", E2E_FAKE_LOCK_FILE=lock)
        self.assertEqual(done.returncode, 3)
        self.assertIn("runtime com.apple.CoreSimulator.SimRuntime.iOS-99-0 is not installed", done.stderr)
        self.assertFalse([c for c in box.calls(box.xcrun) if "create" in c])

    @support.requires_macos("ios/device.sh writes the simulator settings through the host `defaults` command")
    def test_health_stop_and_recover(self):
        box = Sandbox(self, "ios")
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Shutdown\n", box.xcrun)
        self.assertEqual(box.run("health").returncode, 3)
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        box.write("prefs/FAKE-0000-0000-0000-000000000001/-g/AppleLocale", "%s\n" % box.lock["IOS_LOCALE"], box.xcrun)
        self.assertEqual(box.run("health").returncode, 0)
        self.assertEqual(box.run("recover").returncode, 0)
        calls = box.calls(box.xcrun)
        self.assertLess(calls.index("simctl shutdown FAKE-0000-0000-0000-000000000001"),
            calls.index("simctl boot FAKE-0000-0000-0000-000000000001"))
        self.assertEqual(box.run("stop").returncode, 0)
        self.assertEqual(box.run("health").returncode, 3)

    def test_health_reads_a_preference_and_a_simulator_that_does_not_serve_it_is_degraded(self):
        box = Sandbox(self, "ios")
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        done = box.run("health")  # booted, but `defaults read` answers nothing: the preferences daemon stopped serving
        self.assertEqual(done.returncode, 3)
        self.assertIn("simulator degraded", done.stderr)
        self.assertIn("AppleLocale", done.stderr)
        self.assertIn("defaults read -g AppleLocale", "\n".join(box.calls(box.xcrun)))

    def test_a_simulator_that_reads_back_another_locale_is_degraded_too(self):
        box = Sandbox(self, "ios")
        box.write("sim", "FAKE-0000-0000-0000-000000000001 Booted\n", box.xcrun)
        box.write("prefs/FAKE-0000-0000-0000-000000000001/-g/AppleLocale", "fr_FR\n", box.xcrun)
        done = box.run("health")
        self.assertEqual(done.returncode, 3)
        self.assertIn("simulator degraded", done.stderr)

    def test_toolchain_reports_the_installed_versions_without_creating_a_simulator(self):
        box = Sandbox(self, "ios")
        done = box.run("toolchain")
        self.assertEqual(done.returncode, 0, done.stderr)
        for key in ("XCODE_VERSION", "XCODE_BUILD", "IOS_RUNTIME_ID", "IOS_RUNTIME_BUILD", "IOS_DEVICE_TYPE_ID"):
            self.assertEqual(done.json[key], box.lock[key], key)
        self.assertIsNone(done.json["IOS_SIMULATOR_NAME"])
        self.assertFalse([c for c in box.calls(box.xcrun) if "create" in c or "boot" in c])
        stale = box.run("toolchain", FAKE_XCODE_VERSION="26.4")
        self.assertEqual(stale.json["XCODE_VERSION"], "26.4")


if __name__ == "__main__":
    unittest.main()
