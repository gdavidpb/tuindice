# About hand-off triggers: the app leaving the foreground (iOS)

Status: platform-edge note; the scenario `about-platform-edge-triggers` asserts that the share sheet appears and that closing it over a row with a link touches nothing of the app, then taps the other triggers and comes back, but asserts nothing about their hand-off.

Platform-only scope:

- after tapping "Puntúa" (store), "Contacto" (mail) or "Reportar un error", verify the system took over: the app left the foreground (`waitBackgrounded`) and the store, the mail composer or the bug-report composer is in front

Measured on the `TuIndice-E2E` simulator (series, dates, commits and runs in `docs/e2e-mediciones.md`, section 3):

- none of the three triggers takes the app out of the foreground, each one measured on its own: `waitBackgrounded` after the store trigger was still false after 20 s (one run), and so it was after the mail trigger ("Contacto") and after "Reportar un error", 3 runs each, 20 s each (`waitBackgrounded` added temporarily to each trigger in turn); the simulator has no store or mail app to take the foreground
- the share sheet is a popover (`Popover`, with a full-screen `PopoverDismissRegion` behind it) without a close button. The scenario waits for the `PopoverDismissRegion` before it closes the sheet, so a share that opens nothing fails it. The scenario closes it by tapping the dimmed area at the height of the "Creative Commons" row (x = 0.5, y = 0.3 of the screen) and asserts that the sheet went, that About is still in front and that the next tap is answered: the product keeps the app under the sheet from taking touches while the sheet is up (`IosShareTextHandler`), so that tap no longer opens the link (the diagnosis and the measurements are in `docs/e2e-mediciones.md`, section 6).

Covered elsewhere: the host tests of the About presenter (`ShareApp`, `RateOnStore`, `ContactDeveloper`, `ReportBug` reach their handlers).

Reason: the simulator has no store or mail app to take the foreground, so there is nothing for an assertion to wait for; on a device the hand-off is checked by hand.
