package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One section of the requirements: its label, the subjects it lists, and the line it shows when it lists none.
data class RequirementSectionItem(
	val label: UiText,
	val subjects: List<SubjectRowItem>,
	val emptyText: UiText
)
