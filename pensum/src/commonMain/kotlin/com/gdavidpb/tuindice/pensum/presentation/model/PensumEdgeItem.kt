package com.gdavidpb.tuindice.pensum.presentation.model

data class PensumEdgeItem(
	val id: String,
	val fromNodeId: String,
	val toNodeId: String,
	val relationshipType: PensumEdgeRelationshipType,
	val isDisconnected: Boolean = false,
	val points: List<PensumPointItem>
)
