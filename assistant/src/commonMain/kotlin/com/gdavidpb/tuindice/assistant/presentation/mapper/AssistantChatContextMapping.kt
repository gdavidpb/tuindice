package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.ChatContextItem
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

// The chip at the top of the sheet: the screen the student came from and, at most, the one thing of that screen that
// reads on its own, as in «Resumen · Abr - Jul 2026» or «Evaluación · CI4325 PARCIAL 1». A figure never goes in
// alone: without the label the screen gives it, a number says nothing. A screen with no such thing shows its name
// alone. Only the chip reads this: the context the turn sends keeps every field.
internal suspend fun AssistantScreenContext.toChatContextItem(texts: AssistantChatTexts): ChatContextItem {
	val parts = listOfNotNull(texts.screenName(screen), principalText())

	return ChatContextItem(label = parts.joinToString(separator = " · "), context = this)
}

// The principal field of each screen, under the names its visibleFields are declared with.
// A subject goes code first, as the cards show it.
private fun AssistantScreenContext.principalText(): String? {
	return when (screen) {
		AssistantScreen.SUMMARY -> field("currentTermLabel")

		// A subject asked about from its card names the subject; the screen itself names the term on screen.
		AssistantScreen.RECORD -> joined(field("subjectCode"), field("subjectName")) ?: field("termLabel")

		AssistantScreen.SUBJECT_DETAIL -> joined(field("displayCode"), field("name"))

		AssistantScreen.EVALUATION_DETAIL -> field("title")?.let { title ->
			joined(entityId(AssistantEntityKind.SUBJECT), title)
		}

		// What these screens declare are figures or nothing that reads on its own.
		AssistantScreen.STATISTICS,
		AssistantScreen.PENSUM,
		AssistantScreen.EVALUATIONS,
		AssistantScreen.SUBJECT_SEARCH,
		AssistantScreen.ABOUT,
		-> null
	}
}

private fun AssistantScreenContext.field(name: String): String? =
	visibleFields[name]?.trim()?.takeIf { value -> value.isNotEmpty() }

private fun AssistantScreenContext.entityId(kind: AssistantEntityKind): String? =
	entities.firstOrNull { entity -> entity.kind == kind }?.id?.takeIf { id -> id.isNotBlank() }

private fun joined(vararg parts: String?): String? =
	parts.filterNotNull().joinToString(separator = " ").takeIf { text -> text.isNotEmpty() }
