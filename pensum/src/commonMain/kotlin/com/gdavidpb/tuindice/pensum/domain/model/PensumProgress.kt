package com.gdavidpb.tuindice.pensum.domain.model

data class PensumProgress(
	val approvedCredits: Int,
	val nodeStatuses: Map<String, PensumNodeStatus>
)
