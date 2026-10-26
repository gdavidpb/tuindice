package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// conocimiento.cita/1: one entry per cited source, and the link to the record the card names, when it names one.
@Immutable
data class CitationItem(
	override val id: String,
	override val opensSheet: Boolean,
	val sources: List<CitationSourceItem>,
	val recordLink: CardLinkItem?
) : AcademicComponentItem
