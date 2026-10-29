package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

@Immutable
data class TermReportLegendItem(
	val tone: TermReportOutcomeTone,
	val text: UiText
)
