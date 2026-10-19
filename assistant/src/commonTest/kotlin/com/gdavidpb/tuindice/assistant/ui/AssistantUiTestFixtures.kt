package com.gdavidpb.tuindice.assistant.ui

import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatContextItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.presentation.model.IntentDraftItem
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.chatcontract.intent.IntentEffectLine

internal const val INTENT_ID = "intent-1"

internal fun contentState(
	items: List<ChatItem> = emptyList(),
	context: ChatContextItem? = null,
	composer: ChatComposerItem = ChatComposerItem(text = "", isEnabled = true, isSendEnabled = false),
	suggestions: List<ChatSuggestionItem> = emptyList(),
	isGuided: Boolean = false
): AssistantChat.State.Content =
	AssistantChat.State.Content(
		context = context,
		items = items,
		progressLabel = null,
		composer = composer,
		suggestions = suggestions,
		notice = null,
		isGuided = isGuided
	)

internal fun agentMessage(
	id: String = "message-1",
	text: String = "Tu promedio es 16,2.",
	useful: Boolean? = null,
	reported: Boolean = false
): ChatItem.Agent =
	ChatItem.Agent(
		id = id,
		parts = listOf(ChatPartItem.Text(text)),
		useful = useful,
		reported = reported,
		copyText = text
	)

internal fun contextChip(): ChatContextItem =
	ChatContextItem(
		label = "Evaluaciones",
		context = AssistantScreenContext(screen = AssistantScreen.EVALUATIONS)
	)

internal fun intentCard(
	status: IntentCardStatus = IntentCardStatus.Open,
	isDestructive: Boolean = false,
	usesCredential: Boolean = false,
	drafts: List<IntentDraftItem> = emptyList()
): IntentCardItem =
	IntentCardItem(
		intentId = INTENT_ID,
		title = "Agregar evaluación",
		effect = listOf(IntentEffectLine(label = "Parcial 1", detail = "15 %")),
		isDestructive = isDestructive,
		usesCredential = usesCredential,
		confirmLabel = "Aceptar",
		cancelLabel = "Descartar",
		drafts = drafts,
		expiresAtMillis = 0L,
		status = status
	)
