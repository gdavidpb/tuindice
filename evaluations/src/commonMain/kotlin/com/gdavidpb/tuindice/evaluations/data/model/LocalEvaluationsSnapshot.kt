package com.gdavidpb.tuindice.evaluations.data.model

data class LocalEvaluationsSnapshot(
	val anchorRevision: Long,
	val evaluations: List<LocalEvaluation>
)
