package com.gdavidpb.tuindice.assistant.presentation.model

// pensum.requisitos/1: the subject is the label, then one section each for its prerequisites, its corequisites and the
// subjects it unlocks.
data class RequirementsItem(
	override val id: String,
	override val opensSheet: Boolean,
	val subjectName: String,
	// Only the sections that have subjects.
	val sections: List<RequirementSectionItem>,
	val recordLink: CardLinkItem?
) : ChatComponentItem
