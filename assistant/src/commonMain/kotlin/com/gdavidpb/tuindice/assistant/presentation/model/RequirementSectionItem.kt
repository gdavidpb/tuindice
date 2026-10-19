package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One section of the requirements: its label, and the subjects it lists.
data class RequirementSectionItem(
	val label: UiText,
	val subjects: List<SubjectRowItem>
)
