package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * The figure of a block: the big [value] and the [label] that says what it counts, and a [trailing] text after the
 * label when the block has a share to add ("3 %").
 */
@Immutable
data class SummaryStatisticsFigureItem(
	val value: String,
	val label: UiText,
	val trailing: UiText? = null
)
