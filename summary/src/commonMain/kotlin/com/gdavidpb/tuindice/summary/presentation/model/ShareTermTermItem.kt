package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * A closed term the sheet offers: its [key], its [label] ("Ene - Mar 2026"), the [card] that shares it, and whether
 * it is the one [selected].
 */
@Immutable
data class ShareTermTermItem(
	val key: String,
	val label: UiText,
	val card: ShareableTermCardItem,
	val selected: Boolean
)
