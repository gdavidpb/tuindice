package com.gdavidpb.tuindice.evaluations.data.model

data class LocalEvaluationsSnapshot(
	val hasSynced: Boolean,
	val evaluations: List<LocalEvaluation>
)
