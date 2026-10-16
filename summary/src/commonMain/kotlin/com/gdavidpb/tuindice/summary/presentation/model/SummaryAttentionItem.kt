package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/** A line of what needs attention: it names the thing and leads to the screen that owns it. */
@Immutable
data class SummaryAttentionItem(
	val key: String,
	val text: UiText,
	val target: SummaryHubTarget
)
