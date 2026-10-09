# Known defect: a tap on the dimmed area of the share sheet reaches the screen under it (iOS)

Status: known defect of the product on the simulator, documented and not fixed. Three presentation settings were tried and none changed it; the product was left as it was. The scenario `about-platform-edge-triggers` works around it by closing the sheet at a point where the app has nothing to activate.

## What happens

The "Compartir" trigger of About presents the system share sheet as a popover (`Popover`) with a full-screen `PopoverDismissRegion` behind it and no close button. Tapping the dimmed area closes the sheet, and the same tap also reaches whatever the app shows under the finger. With the sheet open, a tap at the height of the "Creative Commons" row of About (x = 0.5, y = 0.3 of the screen) closes the sheet and opens that link in Safari: the app goes to the background and its status bar shows "◂ TuIndice". A tap in the status bar (x = 0.5, y = 0.04) leaves the sheet open; a tap in the empty part of the top bar (x = 0.6, y = 0.1) closes the sheet and leaves About as it was.

## How to reproduce

Run `about-platform-edge-triggers` on iOS with `tapAtScreen(0.5, 0.3)` in place of `tapAtScreen(0.6, 0.1)` in its iOS branch (`ShareSheet.DISMISS_X` and `ShareSheet.DISMISS_Y`), or by hand on the simulator: open About, tap "Compartir", tap the row of a link. 3 of 3 runs showed the app going to Safari, and `waitBackgrounded` passed.

## What was tried (product code `IosShareTextHandler.kt`, one setting at a time, rebuilt each time, the same tap on the same scenario)

| Setting | Result |
|---|---|
| `popoverPresentationController` with `sourceView = topController.view` and `passthroughViews = []` | the defect stays; with a capture after the tap, the app is not in front. The look of the sheet did not change against the base (compared by capture) |
| `modalPresentationStyle = PageSheet` | the same, 3 of 3 |
| `modalPresentationStyle = OverFullScreen` | the same, 3 of 3 |

The look of the sheet was compared by capture only for the first setting; the other two were not compared and probably change it.

## Not tried

- a `sourceRect` for the popover (the variant suggested next to the others)

## Doubts about the diagnosis

- with the second and the third setting, and in the base with the strict version of the scenario, the failure observed is "`PopoverDismissRegion` still visible after 10 s". It agrees with the app being behind Safari, but that was checked with a capture only in the base and in the first setting.
- a first batch with an extra wait between the tap on "Compartir" and the next step gave different results: the outcome depends on the sequence of steps. That batch is not counted.

The measurements are in `docs/e2e-mediciones.md`, section 3. The share sheet is also covered by `conformance-system`.
