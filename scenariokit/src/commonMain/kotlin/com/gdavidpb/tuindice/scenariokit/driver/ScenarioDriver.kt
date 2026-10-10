package com.gdavidpb.tuindice.scenariokit.driver

/**
 * Everything a platform runner implements. All calls are blocking, take milliseconds as
 * `Long`, never throw and answer `false` or `null` when they cannot do what was asked.
 *
 * Two implementations exist: UI Automator out of process on Android (`:scenariorunner`) and XCUITest on iOS
 * (`iosApp/UITests`, through the exported `ScenarioKit` framework). The interpreter owns the policy (waiting for
 * targets, re-reading typed text, deciding what a failure is); a driver performs the primitive it is asked for and
 * never clears the state of the app. The harness cleans the app between scenarios (Android `pm clear`, iOS uninstall
 * plus keychain reset), so a runner started by hand does not start the next scenario from a clean app.
 *
 * `DriverContract` (scenariokit's `contract` package) is the check that a driver honors this contract on a real
 * device; each platform runs it as a test of its own.
 */
interface ScenarioDriver : AppControl, ElementProbe, Gestures, TextEntry, BackendControl, Diagnostics
