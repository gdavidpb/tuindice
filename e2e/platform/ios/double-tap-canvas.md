# Pensum canvas zoom by double tap (iOS)

Status: platform-edge note; `conformance-double-tap-swipe` fires the double tap on iOS but asserts its effect only on Android.

Platform-only scope:

- a double tap on the pensum canvas (`PensumGraphCanvas.kt`, `toggleDoubleTapZoom`) zooms it in and reveals the minimap toggle; a second one zooms back to fit

Measured on the `TuIndice-E2E` simulator (E2c, 40 minutes, one run per delivery, the effect read as `pensum_minimap_toggle` appearing; it does appear on iOS after the zoom-in button, so the tree can show it):

- `coordinate.doubleTap()` at the centre of the canvas (the drivers stage): no effect
- `element.tap(withNumberOfTaps: 2, numberOfTouches: 1)` on the canvas element: no effect
- `element.doubleTap()` on the canvas element: no effect
- `coordinate.doubleTap()` at a quarter of the canvas height, in an empty part of the canvas, so that a node under the finger does not discard the double tap (`isNodeTap`): no effect
- two `coordinate.press(forDuration: 0.02)` one after the other: the second began 422 ms after the first (each XCUITest call costs about 0.4 s), more than the 300 ms the double tap detector of Compose waits for a second tap: no effect

None of the deliveries was run 10 times: none moved the canvas once, so there was no delivery to repeat.

What is not known: whether the double tap zoom works on a real iPhone. It may be a defect of the product on iOS (the same gesture detector works on Android), or the way XCUITest synthesises the two taps (their interval is not exposed). To check by hand: on an iPhone, double tap on an empty part of the pensum canvas and see whether it zooms in.

Reason: no public XCUITest API delivers a double tap that the canvas answers on the simulator, and the interval between the taps cannot be controlled from outside.
