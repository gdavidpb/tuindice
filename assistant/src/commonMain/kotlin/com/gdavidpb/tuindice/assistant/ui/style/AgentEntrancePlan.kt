package com.gdavidpb.tuindice.assistant.ui.style

import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem

/**
 * When each part of an answer starts entering, and when its actions do: the text reveals first, the next part starts
 * as the last word fades in, and every part after it follows [AssistantChatMotion.StaggerMillis] behind the previous.
 */
internal class AgentEntrancePlan(
	val partDelays: List<Int>,
	val actionsDelay: Int
) {
	/** The moment the last thing of the answer has finished entering. */
	val totalMillis: Int = actionsDelay + AssistantChatMotion.MessageEnterMillis
}

internal fun planAgentEntrance(parts: List<ChatPartItem>): AgentEntrancePlan {
	var cursor = 0
	val delays = parts.map { part ->
		val delay = cursor

		cursor += if (part is ChatPartItem.Text) {
			val total = revealTotalMillis(wordRanges(part.text).size).toInt()
			(total - AssistantChatMotion.WordFadeMillis).coerceAtLeast(0) + AssistantChatMotion.StaggerMillis
		} else {
			AssistantChatMotion.StaggerMillis
		}
		delay
	}

	return AgentEntrancePlan(partDelays = delays, actionsDelay = cursor)
}
