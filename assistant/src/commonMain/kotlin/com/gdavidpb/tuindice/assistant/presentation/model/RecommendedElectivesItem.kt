package com.gdavidpb.tuindice.assistant.presentation.model

// materias.recomendadas/1: a row per recommended elective, with the short reason the service gives for it.
data class RecommendedElectivesItem(
	override val id: String,
	override val opensSheet: Boolean,
	val rows: List<ElectiveRowItem>,
	val recordLink: CardLinkItem?
) : AcademicComponentItem
