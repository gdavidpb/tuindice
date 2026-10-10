"""iOS device.sh marks the keyboard's first-use sheet as seen (it covers the keys after a clean boot) and reads it back."""

import unittest

import support  # puts e2e/scripts/shared on sys.path
from test_device import Sandbox

UDID = "FAKE-0000-0000-0000-000000000001"


class IosKeyboardFirstUseSheetTests(unittest.TestCase):
    @support.requires_macos("ios/device.sh writes the simulator preferences with defaults")
    def test_ensure_marks_the_first_use_sheet_as_seen_and_reads_it_back(self):
        box = Sandbox(self, "ios")
        done = box.run("ensure")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(done.json["settings"]["com.apple.keyboard.preferences.DidShowContinuousPathIntroduction"], "1")
        self.assertIn("simctl spawn %s defaults write com.apple.keyboard.preferences DidShowContinuousPathIntroduction -bool true" % UDID,
            box.calls(box.xcrun))

    def test_a_flag_that_does_not_read_back_exits_3(self):
        box = Sandbox(self, "ios")
        box.write("ignore", "com.apple.keyboard.preferences DidShowContinuousPathIntroduction\n", box.xcrun)
        box.write("sim", "%s Booted\n" % UDID, box.xcrun)
        done = box.run("ensure")
        self.assertEqual(done.returncode, 3)
        self.assertIn("com.apple.keyboard.preferences DidShowContinuousPathIntroduction reads back ''", done.stderr)

    @support.requires_macos("ios/device.sh writes the simulator preferences with defaults")
    def test_recover_applies_it_again(self):
        box = Sandbox(self, "ios")
        box.write("sim", "%s Booted\n" % UDID, box.xcrun)
        done = box.run("recover")
        self.assertEqual(done.returncode, 0, done.stderr)
        self.assertEqual(done.json["settings"]["com.apple.keyboard.preferences.DidShowContinuousPathIntroduction"], "1")


if __name__ == "__main__":
    unittest.main()
