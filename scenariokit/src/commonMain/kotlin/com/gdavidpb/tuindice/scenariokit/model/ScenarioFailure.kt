package com.gdavidpb.tuindice.scenariokit.model

/** The first failure of a run; [stepIndex] is -1 when it happened before the first step. */
data class ScenarioFailure(
	val kind: FailureKind,
	val stepIndex: Int,
	val primitive: String,
	val target: String,
	val message: String,
	val expected: String,
	val actual: String,
	val site: Site?
)
