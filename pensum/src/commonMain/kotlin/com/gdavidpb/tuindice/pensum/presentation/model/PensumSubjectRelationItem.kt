package com.gdavidpb.tuindice.pensum.presentation.model

data class PensumSubjectRelationItem(
	val nodeId: String,
	val code: String,
	val name: String,
	val status: PensumNodeStatusDisplay,
	val visualStyle: PensumNodeVisualStyle,
	val relationshipType: PensumEdgeRelationshipType
)
