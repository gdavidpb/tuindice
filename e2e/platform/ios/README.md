# iOS platform E2E edge suites

Use this directory for iOS-only E2E cases that Maestro cannot cover with stable black-box selectors.

Expected cases:

- XCUITest app lifecycle checks.
- Host or simulator behavior such as file opener, camera/photo picker, permissions, share sheet, and external browser.
- Accessibility identifier audits when Compose Multiplatform does not expose a selector Maestro can use reliably.

The root `e2ePlatformIos` task is wired and intentionally succeeds until concrete suites are registered.
