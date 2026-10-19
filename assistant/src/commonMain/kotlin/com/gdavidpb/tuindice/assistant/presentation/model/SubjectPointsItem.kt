package com.gdavidpb.tuindice.assistant.presentation.model

// evaluaciones.puntos/1: a row per subject with its points earned over the points evaluated so far.
data class SubjectPointsItem(
	override val id: String,
	override val opensSheet: Boolean,
	val rows: List<PointsRowItem>,
	val recordLink: CardLinkItem?
) : ChatComponentItem
