# About hand-off triggers: the app leaving the foreground (Android)

Status: platform-edge note for one trigger; the scenario `about-platform-edge-triggers` asserts the hand-off of the other two.

Platform-only scope:

- after tapping "Reportar un error", verify that the bug-report composer took over and the app left the foreground

Measured on the `Pixel_10_Pro_XL` emulator (Android 37, series of 10 with `waitBackgrounded` after each trigger, `--repeat 10`, then 10 more with the final scenario):

- "Puntúa" (store): the app left the foreground 20 of 20 times, in about 1.0 s (Play Store `UnauthenticatedMainActivity`)
- "Contacto" (mail): 20 of 20, about 1.5 s (Gmail `WelcomeTourActivity`, because the emulator has no Gmail account)
- "Reportar un error": 0 of 10. Gmail's `ComposeActivityGmailExternal` opens and finishes itself ~100 ms later (`wm_finish_activity ... app-request` in the logcat of the attempt), so the app is back in front before any poll sees it leave. In E2b the same trigger left the app once and not another time: it depends on the Gmail state of the emulator (first use shows the welcome tour), which the scenario cannot seed.

Covered elsewhere: the host tests of the About presenter (`ReportBug` reaches its handler); the scenario still taps the trigger and checks the app is usable on return.

Reason: the hand-off to a mail composer is only observable while that composer stays open, and Gmail without an account closes it at once.
