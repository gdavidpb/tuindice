package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One subject of a term: its grade, or when it has none, its result; and its badge, when it has one.
data class TermRowItem(
	val code: String,
	val name: String,
	val gradeText: String?,
	val outcomeText: UiText?,
	val badgeText: UiText?
)
