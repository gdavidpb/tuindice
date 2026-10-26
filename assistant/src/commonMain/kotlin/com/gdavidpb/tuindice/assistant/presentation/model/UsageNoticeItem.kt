package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

// sistema.aviso_uso/1: the requests left in each window, and when the quota resets if the service knows it.
@Immutable
data class UsageNoticeItem(
	override val id: String,
	override val opensSheet: Boolean,
	val hourText: UiText,
	val dayText: UiText,
	val monthText: UiText,
	// Null when the service does not know when the quota resets.
	val resetsText: UiText?
) : SistemaComponentItem
