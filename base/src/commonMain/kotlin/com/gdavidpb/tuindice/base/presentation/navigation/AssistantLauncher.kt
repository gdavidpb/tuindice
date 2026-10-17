package com.gdavidpb.tuindice.base.presentation.navigation

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

fun interface AssistantLauncher {
	fun launch(context: AssistantScreenContext)
}
