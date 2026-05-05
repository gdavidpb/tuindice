package com.gdavidpb.tuindice.pensum.domain.model

data class ObservedPensum(
	val selection: PensumSelection,
	val availablePensums: List<PensumOption>,
	val availableModalities: List<PensumModality>,
	val pensum: PensumGraph,
	val approvedCredits: Int,
	val nodeStatuses: Map<String, PensumNodeStatus>
)
