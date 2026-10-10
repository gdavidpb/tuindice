package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.Diagnostics

/** [what] followed by the reason the driver gave for refusing it, when it gave one. Call it right after the refusal. */
internal fun Diagnostics.refused(what: String): String {
	val reason = runCatching { lastRefusal() }.getOrNull()?.takeIf { it.isNotBlank() }
	return if (reason == null) what else "$what: $reason"
}
