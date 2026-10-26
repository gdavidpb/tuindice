package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

// sistema.advertencia/1: a warning about the reason the service gives.
@Immutable
data class WarningItem(
	override val id: String,
	override val opensSheet: Boolean,
	val reasonText: UiText
) : SistemaComponentItem
