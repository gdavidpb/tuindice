package com.gdavidpb.tuindice.assistant.presentation.model

// pensum.avance/1: the share of the credits approved is the figure, with the credits beside it, and a bar of the same
// share, then how many subjects are approved, in progress, available and blocked.
data class PensumProgressItem(
	override val id: String,
	override val opensSheet: Boolean,
	val primaryFigure: CardFigureItem,
	val secondaryFigure: CardFigureItem,
	val progressFraction: Float,
	val statusRows: List<CountRowItem>,
	val recordLink: CardLinkItem?
) : ChatComponentItem
