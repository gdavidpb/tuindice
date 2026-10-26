package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef

// nav.abrir/1: a button that opens its screen in the app, with the label the service gives.
@Immutable
data class OpenScreenItem(
	override val id: String,
	override val opensSheet: Boolean,
	val target: NavTarget,
	val entityRefs: List<EntityRef>,
	val label: String
) : SistemaComponentItem
