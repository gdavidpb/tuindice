package com.gdavidpb.tuindice.scenariokit.model

/** One executed step, in pre-order: a container is recorded before its children. */
data class StepRecord(
	val index: Int,
	val primitive: String,
	val target: String,
	val durationMs: Long,
	val outcome: StepOutcome
)
