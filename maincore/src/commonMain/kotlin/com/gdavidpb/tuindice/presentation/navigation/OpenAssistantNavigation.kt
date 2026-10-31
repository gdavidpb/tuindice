package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.assistant.presentation.navigation.AssistantDestination
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

/**
 * Opens the assistant's chat screen with what the previous screen declared. It is pushed where the user is, and it
 * is not pushed twice when it is already on top. Nothing happens outside a
 * signed-in session.
 */
fun TuIndiceNavigator.openAssistant(context: AssistantScreenContext?) {
	if (rootMode != TuIndiceRootMode.SIGNED_IN) return

	push(AssistantDestination.Chat(context = context))
}
