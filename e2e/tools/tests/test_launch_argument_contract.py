"""scripts/verify-launch-argument-contract.sh: debug launch input never reaches code that ships (A-4, D-12).

The sample sources are written into a throwaway tree, not kept as files: a .kt or .swift sample under the repository
would be scanned by the real script. No gate runs the script yet; F26 hangs it from verifyE2eContract."""

import os
import shutil
import subprocess
import tempfile
import unittest

TESTS = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.realpath(os.path.join(TESTS, "..", "..", ".."))
SCRIPT = os.path.join(ROOT, "scripts", "verify-launch-argument-contract.sh")

DEFINITION_PATH = "maincore/src/commonMain/kotlin/com/gdavidpb/tuindice/debug/DebugLaunchArguments.kt"
# Built from two pieces: the real script scans e2e/ for launch-argument literals, tests included.
PREFIX = "TUINDICE_" + "E2E_"
SEED = PREFIX + "SEED"
UNDECLARED = PREFIX + "UNDECLARED"
DEFINITION = 'object DebugLaunchArguments {\n\tconst val SEED = "%s"\n}\n' % SEED
HOST = "iosApp/Sources/TuIndiceHost/Host.swift"


def swift(body):
    return "import Foundation\nenum Host {\n    static func read() {\n%s\n    }\n}\n" % body


class LaunchContractTests(unittest.TestCase):
    def setUp(self):
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-launch-contract-"))
        self.addCleanup(shutil.rmtree, self.dir, True)
        self.write(DEFINITION_PATH, DEFINITION)
        self.write(HOST, swift("        _ = 1"))

    def write(self, relative, text):
        path = os.path.join(self.dir, relative)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w") as handle:
            handle.write(text)

    def run_script(self, root=None):
        done = subprocess.run(["bash", SCRIPT], env=dict(os.environ, LAUNCH_CONTRACT_ROOT=root or self.dir),
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)
        return done.returncode, done.stdout

    def assertFails(self, rule, fragment=""):
        code, out = self.run_script()
        self.assertEqual(code, 1, out)
        self.assertIn("FAIL [rule %s]" % rule, out)
        self.assertIn(fragment, out)

    def assertPasses(self):
        code, out = self.run_script()
        self.assertEqual(code, 0, out)

    # -- the repository itself ------------------------------------------------------------------------------------
    def test_this_repository_satisfies_the_contract(self):
        code, out = self.run_script(ROOT)
        self.assertEqual(code, 0, out)

    # -- rule 3: Swift reads need a DEBUG frame -------------------------------------------------------------------
    def test_a_read_inside_if_debug_passes(self):
        self.write(HOST, swift("        #if DEBUG\n        _ = ProcessInfo.processInfo.environment[\"K\"]\n        #endif"))
        self.assertPasses()

    def test_each_read_outside_debug_fails(self):
        reads = {
            "environment": '_ = ProcessInfo.processInfo.environment["K"]',
            "arguments": "_ = ProcessInfo.processInfo.arguments",
            "alias of processInfo": "let info = ProcessInfo.processInfo",
            "a new ProcessInfo": "_ = ProcessInfo().environment",
            "CommandLine": "_ = CommandLine.arguments",
            "CommandLine unsafeArgv": "_ = CommandLine.unsafeArgv",
            "CommandLine argc": "_ = CommandLine.argc",
            "getenv": '_ = getenv("K")',
            "UserDefaults string": '_ = UserDefaults.standard.string(forKey: "K")',
            "UserDefaults bool": '_ = UserDefaults.standard.bool(forKey: "K")',
            "UserDefaults object": '_ = UserDefaults.standard.object(forKey: "K")',
            "UserDefaults dictionary": "_ = UserDefaults.standard.dictionaryRepresentation()",
            "UserDefaults alias": "let defaults = UserDefaults.standard",
            "expression split in two lines": "_ = UserDefaults.standard\n            .object(forKey: \"K\")",
        }
        for label, line in reads.items():
            self.write(HOST, swift("        " + line))
            code, out = self.run_script()
            self.assertEqual(code, 1, "%s: %s" % (label, out))
            self.assertIn("FAIL [rule 3]", out, label)

    def test_a_write_to_user_defaults_is_not_launch_input(self):
        self.write(HOST, swift('        UserDefaults.standard.set(["es"], forKey: "AppleLanguages")'))
        self.assertPasses()

    def test_a_comment_is_not_a_read(self):
        self.write(HOST, swift("        // ProcessInfo.processInfo.environment is debug only"))
        self.assertPasses()

    def test_conditions_with_a_negation_or_an_or_are_not_debug_frames(self):
        for condition in ("!DEBUG", "!(DEBUG)", "DEBUG || TESTING", "TESTING || DEBUG", "DEBUG && !TESTING"):
            self.write(HOST, swift("        #if %s\n        _ = ProcessInfo.processInfo.arguments\n        #endif" % condition))
            code, out = self.run_script()
            self.assertEqual(code, 1, "#if %s: %s" % (condition, out))
            self.assertIn("FAIL [rule 3]", out)

    def test_the_else_of_if_not_debug_is_a_debug_frame_and_the_else_of_if_debug_is_not(self):
        self.write(HOST, swift("        #if !DEBUG\n        _ = 1\n        #else\n        _ = ProcessInfo.processInfo.arguments\n        #endif"))
        self.assertPasses()
        self.write(HOST, swift("        #if DEBUG\n        _ = 1\n        #else\n        _ = ProcessInfo.processInfo.arguments\n        #endif"))
        self.assertFails("3")

    def test_a_nested_frame_keeps_the_outer_debug(self):
        self.write(HOST, swift("        #if DEBUG\n        #if os(iOS)\n        _ = CommandLine.arguments\n        #endif\n        #endif"))
        self.assertPasses()

    def test_the_swift_host_names_the_launch_arguments_class_only_inside_if_debug(self):
        named = "        let arguments = DebugLaunchArguments.companion.parse(values: [])"
        self.write(HOST, swift("        #if DEBUG\n" + named + "\n        #endif"))
        self.assertPasses()
        self.write(HOST, swift(named))
        self.assertFails("5", "DebugLaunchArguments named outside #if DEBUG")
        self.write(HOST, swift("        #if !DEBUG\n" + named + "\n        #endif"))
        self.assertFails("5")

    def test_a_swift_name_that_merely_contains_the_class_name_is_not_the_class(self):
        self.write(HOST, swift("        let applyDebugLaunchArgumentsLater = 1"))
        self.assertPasses()

    # -- rule 4: Kotlin for iOS --------------------------------------------------------------------------------------
    def test_kotlin_for_ios_reading_launch_input_fails(self):
        for source_set in ("iosMain", "appleMain", "nativeMain", "iosSimulatorArm64Main"):
            for line in ("val v = NSProcessInfo.processInfo.environment", "val d = NSUserDefaults.standardUserDefaults",
                    'val e = platform.posix.getenv("K")'):
                self.write("auth/src/%s/kotlin/Reader.kt" % source_set, "package x\nfun read() {\n\t%s\n}\n" % line)
                code, out = self.run_script()
                self.assertEqual(code, 1, "%s %s: %s" % (source_set, line, out))
                self.assertIn("FAIL [rule 4]", out)
            os.remove(os.path.join(self.dir, "auth/src/%s/kotlin/Reader.kt" % source_set))

    def test_kotlin_that_merely_names_a_settings_factory_passes(self):
        self.write("maincore/src/iosMain/kotlin/Module.kt", "import com.russhwolf.settings.NSUserDefaultsSettings\nval f = NSUserDefaultsSettings.Factory()\n")
        self.assertPasses()

    # -- rule 5: the constants stay out of shipped code ---------------------------------------------------------------
    def test_the_constants_are_only_used_where_the_contract_allows(self):
        use = "import com.gdavidpb.tuindice.debug.DebugLaunchArguments\nval k = DebugLaunchArguments.SEED\n"
        allowed = ("app/src/debug/kotlin/E2eSeedBridge.kt", "scenarios/src/commonMain/kotlin/Start.kt",
            "maincore/src/iosMain/kotlin/com/gdavidpb/tuindice/ui/IosAppHostBootstrap.kt",
            "maincore/src/commonTest/kotlin/DebugLaunchArgumentsTest.kt", "scenarios/src/androidHostTest/kotlin/StartTest.kt")
        for path in allowed:
            self.write(path, use)
        self.assertPasses()
        for path in ("app/src/main/kotlin/Shipping.kt", "summary/src/commonMain/kotlin/Screen.kt",
                "maincore/src/iosMain/kotlin/OtherIosFile.kt", "app/src/release/kotlin/Release.kt"):
            self.write(path, use)
            code, out = self.run_script()
            self.assertEqual(code, 1, "%s: %s" % (path, out))
            self.assertIn("FAIL [rule 5]", out)
            self.assertIn(path, out)
            os.remove(os.path.join(self.dir, path))

    # -- rules 1 and 2 ------------------------------------------------------------------------------------------------
    def test_a_literal_outside_the_definition_fails_even_under_a_package_named_test(self):
        self.write("app/src/main/kotlin/com/example/test/Literal.kt", 'val k = "%s"\n' % SEED)
        self.assertFails("1", "Literal.kt")
        os.remove(os.path.join(self.dir, "app/src/main/kotlin/com/example/test/Literal.kt"))
        self.write("app/src/test/kotlin/Literal.kt", 'val k = "%s"\n' % SEED)
        self.assertPasses()

    def test_a_literal_in_configuration_and_script_files_outside_the_allowed_paths_fails(self):
        # D-12: rule 1 read only .kt and .swift; the same literal in a plist, a scheme, an xcconfig, a build script, a
        # workflow or a helper script is also a launch argument written by hand.
        files = ("iosApp/Resources/Info.plist", "iosApp/TuIndiceHost.xcodeproj/xcshareddata/xcschemes/Host.xcscheme",
            "iosApp/Config/Debug.xcconfig", "app/build.gradle.kts", "settings.gradle", ".github/workflows/ci.yml",
            ".github/workflows/ci.yaml", "scripts/helper.sh", "scripts/helper.py", "iosApp/scripts/helper.rb")
        for path in files:
            self.write(path, "key = %s\n" % SEED)
            code, out = self.run_script()
            self.assertEqual(code, 1, "%s: %s" % (path, out))
            self.assertIn("FAIL [rule 1]", out)
            self.assertIn(path, out)
            os.remove(os.path.join(self.dir, path))
        self.assertPasses()

    def test_the_allowed_paths_may_carry_the_literal_in_any_file_type(self):
        # The definition, e2e/ (rule 2 checks it is declared), the iOS UI tests, .codex/ and the script with its tests.
        for path in ("e2e/scripts/ios/run.sh", "e2e/tools/tests/test_x.py", "iosApp/UITests/Config.plist", ".codex/skills/x/run.sh",
                "scripts/verify-launch-argument-contract.sh", "app/src/test/resources/x.yml"):
            self.write(path, "key = %s\n" % SEED)
        self.assertPasses()

    def test_an_undeclared_key_in_a_large_asset_is_not_skipped(self):
        # D-12: a 2 MB cut-off silently skipped files; the real catalog weighs 988 KB and grows.
        self.write("e2e/catalog/big.json", ('{"x":"%s"}\n' % ("a" * 100)) * 25000 + UNDECLARED + "\n")
        self.assertGreater(os.path.getsize(os.path.join(self.dir, "e2e/catalog/big.json")), 2000 * 1024)
        self.assertFails("2", UNDECLARED)

    def test_a_declared_key_in_the_assets_passes(self):
        self.write("e2e/catalog/ok.json", '{"arg":"%s"}\n' % SEED)
        self.assertPasses()


if __name__ == "__main__":
    unittest.main()
