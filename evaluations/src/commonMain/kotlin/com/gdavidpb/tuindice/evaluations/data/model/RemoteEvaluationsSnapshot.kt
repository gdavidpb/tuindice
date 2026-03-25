package com.gdavidpb.tuindice.evaluations.data.model

data class RemoteEvaluationsSnapshot(
	val anchorRevision: Long,
	val evaluations: List<RemoteEvaluation>
)
