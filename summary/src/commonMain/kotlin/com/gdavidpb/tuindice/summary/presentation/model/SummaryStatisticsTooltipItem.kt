package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/** What a point of a chart shows when it is open: a [title] and the lines under it. */
@Immutable
data class SummaryStatisticsTooltipItem(
	val title: UiText,
	val lines: List<UiText> = emptyList()
)
