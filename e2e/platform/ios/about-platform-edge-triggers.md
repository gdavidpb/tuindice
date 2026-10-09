# About hand-off triggers: the app leaving the foreground (iOS)

Status: platform-edge note; the scenario `about-platform-edge-triggers` taps every trigger and comes back, but asserts nothing about the hand-off itself.

Platform-only scope:

- after tapping "Puntúa" (store), "Contacto" (mail) or "Reportar un error", verify the system took over: the app left the foreground (`waitBackgrounded`) and the store, the mail composer or the bug-report composer is in front
- after tapping "Compartir", verify that closing the share sheet touches nothing of the app

Measured on the `TuIndice-E2E` simulator:

- none of the three triggers takes the app out of the foreground: `waitBackgrounded` after the store trigger was still false after 20 s (E2b); the simulator has no store app to take the foreground (the mail and bug-report triggers were not measured one by one: the same absence of a handler is assumed, and the scenario passes 3 of 3 without any of them leaving the app)
- the share sheet is a popover (`Popover`, with a full-screen `PopoverDismissRegion` behind it) without a close button. Tapping the dimmed area closes the sheet and the same tap also reaches what the app shows under the finger: a tap at the height of the "Creative Commons" row (y = 0.3 of the screen) closed the sheet and opened that link in Safari (the app went to the background, its status bar showed "◂ TuIndice"); a tap in the empty part of the top bar (x = 0.6, y = 0.1) closed the sheet and left the About screen as it was. The scenario uses that second point. The product presents the sheet with the default presentation (`IosShareTextHandler` and the bridge's `shareText`), with no source view and no pass-through setting.

Covered elsewhere: the host tests of the About presenter (`ShareApp`, `RateOnStore`, `ContactDeveloper`, `ReportBug` reach their handlers).

Reason: the simulator has no store or mail app to take the foreground, so there is nothing for an assertion to wait for; on a device the hand-off is checked by hand.
