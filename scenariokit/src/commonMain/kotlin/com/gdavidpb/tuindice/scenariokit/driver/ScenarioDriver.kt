package com.gdavidpb.tuindice.scenariokit.driver

/**
 * Everything a platform runner implements. All calls are blocking, take milliseconds as
 * `Long`, never throw and answer `false` or `null` when they cannot do what was asked.
 */
interface ScenarioDriver : AppControl, ElementProbe, Gestures, TextEntry, BackendControl, Diagnostics
