package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.ChatContextItem
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

// The chip at the top of the sheet: the screen the student came from and what that screen showed, as in
// «Evaluaciones · CI4325 Parcial 1».
internal suspend fun AssistantScreenContext.toChatContextItem(texts: AssistantChatTexts): ChatContextItem {
	val shown = visibleFields.values.filter { value -> value.isNotBlank() }.joinToString(separator = " ")
	val parts = listOf(texts.screenName(screen), shown).filter { part -> part.isNotBlank() }

	return ChatContextItem(label = parts.joinToString(separator = " · "), context = this)
}
