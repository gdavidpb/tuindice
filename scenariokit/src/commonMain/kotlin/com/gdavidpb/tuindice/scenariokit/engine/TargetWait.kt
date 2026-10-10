package com.gdavidpb.tuindice.scenariokit.engine

import com.gdavidpb.tuindice.scenariokit.driver.ElementProbe
import com.gdavidpb.tuindice.scenariokit.model.FailureKind
import com.gdavidpb.tuindice.scenariokit.model.Query
import com.gdavidpb.tuindice.scenariokit.model.Timeouts
import com.gdavidpb.tuindice.scenariokit.model.describe

/** Waits for a step's own target; null when it is there (or when the step has no target). */
internal fun ElementProbe.awaitTarget(q: Query?, timeoutMs: Long = Timeouts.Action): StepResult.Failed? =
	if (q == null || waitVisible(q, timeoutMs)) {
		null
	} else {
		StepResult.Failed(FailureKind.STEP_TIMEOUT, "${q.describe()} was not visible within $timeoutMs ms")
	}
