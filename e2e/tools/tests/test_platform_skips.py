"""The macOS-only tests skip off macOS with their reason, never silently (F26, D-8)."""

import sys
import unittest
from unittest import mock

import support


class RequiresMacosTests(unittest.TestCase):
    def decorated(self, platform):
        with mock.patch.object(sys, "platform", platform):
            return support.requires_macos("it needs `defaults`")(lambda: None)

    def test_a_reason_is_mandatory(self):
        for reason in ("", "   ", None):
            with self.assertRaises(ValueError):
                support.requires_macos(reason)

    def test_off_macos_the_test_is_skipped_and_the_message_carries_the_reason(self):
        test = self.decorated("linux")

        self.assertTrue(test.__unittest_skip__)
        self.assertEqual(test.__unittest_skip_why__, "macOS only: it needs `defaults`")

    def test_on_macos_the_test_runs(self):
        self.assertFalse(getattr(self.decorated("darwin"), "__unittest_skip__", False))


if __name__ == "__main__":
    unittest.main()
