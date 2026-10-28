# Settings and About hand-off triggers: the app leaving the foreground (Android)

Status: platform-edge note for the two mail triggers; the scenario `about-platform-edge-triggers` asserts that the share chooser appears and the hand-off of the store, and taps the two mail triggers.

Platform-only scope:

- after tapping "Reportar un error" or "Contacto", verify that the mail composer took over and the app left the foreground

Measured on the `Pixel_10_Pro_XL` emulator (Android 37, series of 10 with `waitBackgrounded` after each trigger, `--repeat 10`, then 10 more with the final scenario; dates, commits and runs in `docs/e2e-mediciones.md`, section 3):

- "Puntúa" (store): the app left the foreground 20 of 20 times, in about 1.0 s (Play Store `UnauthenticatedMainActivity`)
- "Contacto" (mail): 20 of 20, about 1.5 s (Gmail `WelcomeTourActivity`, because the emulator has no Gmail account)
- "Reportar un error": 0 of 10. Gmail's `ComposeActivityGmailExternal` opens and finishes itself ~100 ms later (`wm_finish_activity ... app-request` in the logcat of the attempt), so the app is back in front before any poll sees it leave. In an earlier series the same trigger left the app once and not another time: it depends on the Gmail state of the emulator (first use shows the welcome tour), which the scenario cannot seed.
- "Contacto" after "Reportar un error" in the same run: 0 of 2 (evidence of 2026-10-28, commit 17c536e). The scenario now taps "Reportar un error" first, so Gmail is past its welcome tour when "Contacto" opens it, and its composer finishes itself the same way.

Covered elsewhere: the host tests of the Settings and About machines (`ReportBug` and `ContactDeveloper` reach their handlers); the scenario still taps both triggers and checks the app is usable on return.

Reason: the hand-off to a mail composer is only observable while that composer stays open, and Gmail without an account closes it at once.
