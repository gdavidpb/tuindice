package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * A compact figure of a block: its [value], a number or a short ratio such as "44/46", and the short [label] that says
 * what it counts, such as "al primer intento". It takes the place of a sentence written under a chart.
 */
@Immutable
data class SummaryStatisticsFactItem(
	val value: String,
	val label: UiText
)
