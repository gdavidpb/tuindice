package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// pensum.impacto/1: the subject is the label, and the terms it delays are the figure, then the chain of subjects that
// depend on it, each with its status.
data class SubjectImpactItem(
	override val id: String,
	override val opensSheet: Boolean,
	val subjectName: String,
	val termsFigure: CardFigureItem,
	val chain: List<SubjectRowItem>,
	val recordLink: CardLinkItem?
) : ChatComponentItem
