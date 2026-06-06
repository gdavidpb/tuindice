package com.gdavidpb.tuindice.pensum.presentation.model

data class PensumScreenModel(
	val careerName: String,
	val selection: PensumScreenSelection,
	val pensumOptions: List<PensumOptionItem>,
	val modalityOptions: List<PensumModalityItem>,
	val progressPercent: Int,
	val approvedCredits: Int,
	val totalCredits: Int,
	val canvas: PensumCanvasItem,
	val terms: List<PensumTermItem>,
	val nodes: List<PensumNodeItem>,
	val edges: List<PensumEdgeItem>
)
