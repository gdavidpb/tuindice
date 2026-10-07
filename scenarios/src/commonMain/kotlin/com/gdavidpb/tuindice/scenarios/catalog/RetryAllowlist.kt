package com.gdavidpb.tuindice.scenarios.catalog

/**
 * The scenarios allowed to wrap steps in `retry`. Empty on purpose: a step that fails intermittently is
 * a defect to fix, not to retry. Adding an id here needs the owner's agreement.
 */
object RetryAllowlist {
	val scenarioIds: Set<String> = emptySet()
}
