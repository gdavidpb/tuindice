package com.gdavidpb.tuindice.scenariokit.engine

import kotlin.time.TimeSource

/** Time sources of one run; tests swap them for a virtual clock that `pause` advances. */
internal class Clocks(
	val timeSource: TimeSource = TimeSource.Monotonic,
	val nowIso: () -> String = ::systemNowIso
)
