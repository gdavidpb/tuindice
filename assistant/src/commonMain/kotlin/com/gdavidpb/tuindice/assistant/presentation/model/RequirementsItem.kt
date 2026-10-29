package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// pensum.requisitos/1: the subject is the label, then one section each for its prerequisites, its corequisites and the
// subjects it unlocks.
@Immutable
data class RequirementsItem(
	override val id: String,
	override val opensSheet: Boolean,
	val subjectName: String,
	// The three sections, empty ones included: an empty section says so in its own line.
	val sections: List<RequirementSectionItem>,
	val recordLink: CardLinkItem?
) : PensumPlanComponentItem
