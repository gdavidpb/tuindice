package com.gdavidpb.tuindice.assistant.presentation.model

// informe.simulacion/1: the index of the simulated term, with the simulated cumulative index beside it, then one row
// per simulated grade.
data class SimulationItem(
	override val id: String,
	override val opensSheet: Boolean,
	val primaryFigure: CardFigureItem?,
	val secondaryFigure: CardFigureItem?,
	val changes: List<SimulatedGradeItem>
) : RecordComponentItem
