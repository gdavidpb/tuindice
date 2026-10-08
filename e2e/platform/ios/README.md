# iOS platform edges

Notes on iOS behavior that no scenario drives, because it sits behind an OS surface or a device state the iOS driver
(`iosApp/UITests/`, XCUITest) cannot reach or seed. No task runs these files; they are the record of what an edge test
would verify, what covers it today and why no scenario does.

Each note has the same parts: the scope an edge test would have, what covers the behavior elsewhere (host and UI tests),
and the reason no scenario does. `ActionDispositions.kt` in `scenarios/` names the notes it relies on, and an action
listed there as a platform edge never also appears in a scenario's `covers`.

A behavior that a scenario can drive does not belong here: it goes in the catalog (`e2e/README.md`, "Add a scenario").
