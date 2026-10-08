"""verify-e2e-vocabulary.sh: no word of the retired runner and no Gradle task name that does not exist.

The word and the task-like names below are built from two halves: this file is scanned by the gate it tests, and it
needs no exception of its own.
"""

import os
import shutil
import subprocess
import tempfile
import unittest

TESTS = os.path.dirname(os.path.abspath(__file__))
SCRIPT = os.path.join(TESTS, "..", "verify", "verify-e2e-vocabulary.sh")
SCRIPT_PATH = os.path.realpath(SCRIPT)
ROOT = os.path.realpath(os.path.join(TESTS, "..", "..", ".."))

WORD = "Mae" + "stro"
VERIFY_THING = "verify" + "Thing"
RUN_THING = "e2e" + "Thing"
RUN_GHOST = "e2e" + "Ghost"
SYNC_GHOST = "sync" + "Ghost"
VERIFY_GHOST = "verify" + "Ghost"
VERIFY_ELSEWHERE = "verify" + "Elsewhere"
RETIRED_RUN = "e2e" + "RetiredRun"
REGISTRY = os.path.join("gradle", "e2e-tasks.gradle.kts")
SELF = os.path.join("e2e", "tools", "verify", "verify-e2e-vocabulary.sh")
RETIRED_IDENTIFIERS = ["SEED_" + "STATE", "e2e" + "Mae" + "stro", "e2e" + "Platform", "Migration" + "Progress",
    "validate-e2e-" + "contract", "run_sequential_" + "evidence", "flow-" + "catalog", "E2E_STRICT_" + "IOS"]


def init_repository(tree):
    subprocess.run(["git", "-C", tree, "init", "-q"], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)


class VocabularyGateTests(unittest.TestCase):
    def setUp(self):
        self.tree = tempfile.mkdtemp(prefix="e2e-vocabulary-")
        self.addCleanup(shutil.rmtree, self.tree, True)
        init_repository(self.tree)
        self.put(REGISTRY, 'tasks.register("%s") {}\ntasks.register<Exec>("%s") {}\n' % (VERIFY_THING, RUN_THING))
        self.put("docs/guide.md", "Run `./gradlew %s` and then `./gradlew %s`.\n" % (VERIFY_THING, RUN_THING))
        self.give_every_exception_its_hit()

    def declared_exceptions(self):
        done = subprocess.run(["bash", SCRIPT, "--print-exceptions"], stdout=subprocess.PIPE, universal_newlines=True,
            check=True)
        return [line.split("|") for line in done.stdout.splitlines() if line]

    def give_every_exception_its_hit(self):
        """An exception without a hit is stale, so the tree starts with one hit behind each declared exception."""
        tokens = []
        for kind, subject in self.declared_exceptions():
            if kind == "word-path":
                self.append(subject, "# leftovers of the %s runner\n" % WORD.lower())
            elif kind == "task-path":
                self.append(subject, "# retired: %s\n" % RETIRED_RUN)
            elif kind == "retired-path":
                self.append(subject, "# retired: %s\n" % RETIRED_IDENTIFIERS[0])
            else:
                tokens.append(subject)
        self.put("docs/exceptions.md", "".join("`%s`\n" % token for token in tokens))

    def put(self, path, text):
        full = os.path.join(self.tree, path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, "w") as handle:
            handle.write(text)

    def append(self, path, text):
        full = os.path.join(self.tree, path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, "a") as handle:
            handle.write(text)

    def verify(self):
        done = subprocess.run(["bash", SCRIPT, "--root", self.tree], stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            universal_newlines=True)
        return done.returncode, done.stdout + done.stderr

    def test_a_tree_with_the_current_vocabulary_passes(self):
        code, out = self.verify()
        self.assertEqual(code, 0, out)

    def test_the_word_fails_in_any_case_and_names_file_and_line(self):
        for spelling in (WORD, WORD.lower(), WORD.upper()):
            self.put("docs/history.md", "first line\nThe flows ran on %s once.\n" % spelling)
            code, out = self.verify()
            self.assertEqual(code, 1, "%s: %s" % (spelling, out))
            self.assertIn("docs/history.md:2", out)

    def test_the_word_fails_in_code_and_scripts_too(self):
        self.put("e2e/scripts/shared/helper.sh", "# runs %s\n" % WORD)
        self.put("scenarios/Some.kt", "// the old %s flow\n" % WORD)
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("e2e/scripts/shared/helper.sh:1", out)
        self.assertIn("scenarios/Some.kt:1", out)

    def test_a_file_name_with_the_word_fails(self):
        self.put("docs/%s-notes.md" % WORD.lower(), "nothing here\n")
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("%s-notes.md" % WORD.lower(), out)

    def test_a_task_that_no_build_file_registers_fails_and_is_named(self):
        self.put("docs/guide.md", "Run `./gradlew %s` or `./gradlew %s`.\n" % (VERIFY_THING, RUN_GHOST))
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("docs/guide.md:1", out)
        self.assertIn(RUN_GHOST, out)
        self.assertNotIn(VERIFY_THING, out)

    def test_every_task_family_is_checked(self):
        self.put("docs/guide.md", "`%s` `%s` `%s`\n" % (SYNC_GHOST, VERIFY_GHOST, RUN_GHOST))
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        for name in (SYNC_GHOST, VERIFY_GHOST, RUN_GHOST):
            self.assertIn(name, out)

    def test_a_task_registered_in_any_build_file_counts_as_existing(self):
        self.put("docs/guide.md", "`./gradlew :feature:%s`\n" % VERIFY_ELSEWHERE)
        self.put("feature/build.gradle.kts", 'tasks.register("%s") {}\n' % VERIFY_ELSEWHERE)
        code, out = self.verify()
        self.assertEqual(code, 0, out)

    def test_kotlin_identifiers_are_not_task_names(self):
        self.put("auth/Sync.kt", "val %s = %s.%s\n" % ("sync" + "Status", "sync" + "Repository", "sync" + "State"))
        code, out = self.verify()
        self.assertEqual(code, 0, out)

    def test_a_glob_over_a_family_is_not_a_task_name(self):
        self.put("docs/guide.md", "The %s* tasks and the %s* runs.\n" % ("verify" + "Thin", "e2e" + "Any"))
        code, out = self.verify()
        self.assertEqual(code, 0, out)

    def test_a_declared_exception_passes_only_in_its_own_path(self):
        code, out = self.verify()
        self.assertEqual(code, 0, out)

        self.put(os.path.join("e2e", "tools", "other.py"), "LEGACY = 'build/e2e/%s-*.log'\n" % WORD.lower())
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("e2e/tools/other.py:1", out)
        self.assertNotIn("e2e-retention.py:1", out)

    def test_an_exception_that_no_longer_has_a_hit_is_reported_as_stale(self):
        retention = os.path.join("e2e", "tools", "e2e-retention.py")
        self.put(retention, "LEGACY = 'build/e2e/checkpoints'\n")
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("stale exception", out)
        self.assertIn("e2e/tools/e2e-retention.py", out)

    def test_a_token_exception_that_appears_nowhere_is_reported_as_stale(self):
        self.put("docs/exceptions.md", "nothing declared is mentioned here\n")
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("stale exception", out)

    def test_the_script_does_not_keep_its_own_token_exceptions_alive(self):
        # The script is a versioned file of the tree it scans, and its own text spells the tokens it excepts. A copy of it
        # in the tree must not count as the use that keeps an exception from being stale.
        with open(SCRIPT_PATH) as handle:
            self.put(SELF, handle.read())
        tokens = [subject for kind, subject in self.declared_exceptions() if kind == "task-token" and subject != "verify" + "E2e"]
        self.put("docs/exceptions.md", "".join("`%s`\n" % token for token in tokens))
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("stale exception: %s appears in no file (task-token)" % ("verify" + "E2e"), out)

    def test_no_token_exception_describes_a_runner_argument_that_does_not_exist(self):
        self.assertNotIn("e2e" + "Trace", [subject for _kind, subject in self.declared_exceptions()])

    def test_every_retired_identifier_fails_wherever_it_reappears(self):
        for identifier in RETIRED_IDENTIFIERS:
            with self.subTest(identifier=identifier):
                self.put("docs/back.md", "first\nuses %s again\n" % identifier)
                code, out = self.verify()
                self.assertEqual(code, 1, out)
                self.assertIn("docs/back.md:2", out)
                self.assertIn(identifier, out)
                os.remove(os.path.join(self.tree, "docs", "back.md"))
        self.put("scenarios/Some.kt", 'val key = "TUINDICE_E2E_%s"\n' % RETIRED_IDENTIFIERS[0])
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("scenarios/Some.kt:1", out)

    def test_a_retired_identifier_passes_only_in_the_declared_files_that_deny_it(self):
        declared = [subject for kind, subject in self.declared_exceptions() if kind == "retired-path"]
        self.assertTrue(declared)
        code, out = self.verify()
        self.assertEqual(code, 0, out)
        self.put(declared[0], "# no longer names anything retired\n")
        code, out = self.verify()
        self.assertEqual(code, 1, out)
        self.assertIn("stale exception: %s" % declared[0], out)

    def test_a_file_that_is_not_versioned_is_not_scanned(self):
        self.put("build/out.txt", "%s\n" % WORD)
        self.put(".gitignore", "build/\n")
        code, out = self.verify()
        self.assertEqual(code, 0, out)

    def test_a_missing_root_is_a_usage_error(self):
        done = subprocess.run(["bash", SCRIPT, "--root", os.path.join(self.tree, "nowhere")], stdout=subprocess.PIPE,
            stderr=subprocess.PIPE, universal_newlines=True)
        self.assertEqual(done.returncode, 2, done.stdout + done.stderr)

    def test_this_repository_passes(self):
        done = subprocess.run(["bash", SCRIPT], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            universal_newlines=True)
        self.assertEqual(done.returncode, 0, done.stdout + done.stderr)

    def test_it_is_part_of_the_harness_checks(self):
        with open(os.path.join(TESTS, "run-harness-tests.sh")) as handle:
            text = handle.read()
        self.assertIn("verify-e2e-vocabulary.sh", text)


if __name__ == "__main__":
    unittest.main()
