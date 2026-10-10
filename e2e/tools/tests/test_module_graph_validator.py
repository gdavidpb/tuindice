"""scripts/validate-module-graph.sh refuses modules the graph tooling cannot read (D-18): the module graph, the E2E
fingerprint and the change detector all read module names as ":[a-z]+", so a module named otherwise would stay out of
the three without anyone noticing."""

import os
import shutil
import subprocess
import tempfile
import unittest

TESTS = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.realpath(os.path.join(TESTS, "..", "..", ".."))
VALIDATOR = os.path.join(ROOT, "scripts", "validate-module-graph.sh")


class ModuleGraphValidatorTests(unittest.TestCase):
    def setUp(self):
        self.dir = os.path.realpath(tempfile.mkdtemp(prefix="e2e-module-graph-"))
        self.addCleanup(shutil.rmtree, self.dir, True)
        os.makedirs(os.path.join(self.dir, "scripts"))
        shutil.copy(VALIDATOR, os.path.join(self.dir, "scripts"))
        self.write("scripts/module-graph.txt", "base=-\napp=:base\n")
        self.write("settings.gradle.kts", 'plugins {\n\tid("x") version "1.0.0"\n}\ninclude(\n\t":app",\n\t":base"\n)\n')
        self.write("base/build.gradle.kts", "")
        self.write("app/build.gradle.kts", 'dependencies {\n\timplementation(project(":base"))\n}\n')

    def write(self, relative, text):
        path = os.path.join(self.dir, relative)
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w") as handle:
            handle.write(text)

    def validate(self):
        done = subprocess.run(["bash", os.path.join(self.dir, "scripts", "validate-module-graph.sh")],
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)
        return done.returncode, done.stdout

    def test_a_consistent_graph_passes(self):
        code, out = self.validate()
        self.assertEqual(code, 0, out)

    def test_an_include_the_tooling_cannot_read_fails_even_when_the_graph_lists_it(self):
        self.write("settings.gradle.kts", 'include(\n\t":app",\n\t":base",\n\t":data-layer"\n)\n')
        self.write("scripts/module-graph.txt", "base=-\napp=:base\n")
        code, out = self.validate()
        self.assertEqual(code, 1, out)
        self.assertIn("FAIL [:data-layer]", out)

    def test_a_module_with_a_digit_or_an_uppercase_letter_fails(self):
        for name in (":base2", ":Base"):
            self.write("settings.gradle.kts", 'include(\n\t":app",\n\t":base",\n\t"%s"\n)\n' % name)
            code, out = self.validate()
            self.assertEqual(code, 1, name + out)
            self.assertIn("FAIL [%s]" % name, out)

    def test_a_dependency_on_an_unreadable_project_fails(self):
        self.write("app/build.gradle.kts", 'dependencies {\n\timplementation(project(":base"))\n\timplementation(project(":data-layer"))\n}\n')
        code, out = self.validate()
        self.assertEqual(code, 1, out)
        self.assertIn("[app]: depends on a project the graph tooling cannot read", out)


if __name__ == "__main__":
    unittest.main()
