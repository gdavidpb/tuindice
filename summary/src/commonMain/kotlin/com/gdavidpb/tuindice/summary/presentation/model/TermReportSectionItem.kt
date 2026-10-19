package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable

/** One section of what the assistant wrote: its title and its text, both as the model wrote them. */
@Immutable
data class TermReportSectionItem(
	val title: String,
	val text: String
)
