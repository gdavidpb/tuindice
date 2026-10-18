package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.presentation.mapper.lastAgentSuggestions
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.message.SuggestedQuestions

// The extended state of the chat sheet: what the table keeps besides its state. The conversation is the one the store
// observed; the rest is what the student did on the sheet and what the outcomes of their requests left. It is written
// only from the table's rows, which run one at a time, so it needs no lock.
internal class AssistantChatSession {
	var openedContext: AssistantScreenContext? = null
	var conversation: AssistantConversation? = null
	var guidedByMode = false
	var guidedByServer = false
	var lastFailedSend: FailedSend? = null
	var turnFailure: TurnFailure? = null
	var quota: Quota? = null
	var restriction: Restriction? = null
	var pendingFailedText: String? = null

	// The suggestion of the turn that was accepted last: a run that fails keeps it for the retry.
	var followedSuggestionId: String? = null
	var photoExtracting = false
	val usefulByMessage = mutableMapOf<String, Boolean>()
	val selectionByDraft = mutableMapOf<Pair<String, String>, Boolean>()
	val runningIntents = mutableSetOf<String>()
	val failedMessages = mutableSetOf<String>()

	fun isRunning(): Boolean = conversation?.activeTurn?.status == AssistantTurnPhase.RUNNING

	// The mode the server or the configuration says the assistant is in. Only a mode that says on releases the guided lock
	// that a refused guided send set: an accepted turn does not.
	fun modeSaid(guided: Boolean) {
		guidedByMode = guided

		if (!guided) guidedByServer = false
	}

	// The replies the chat shows come first. A question of the shared catalog is sent with its own text even when it is
	// not shown, because the service accepts those in guided mode.
	fun suggestionText(suggestionId: String): String? {
		val shown = conversation?.lastAgentSuggestions()?.firstOrNull { suggestion -> suggestion.id == suggestionId }

		return shown?.text ?: SuggestedQuestions.byId(suggestionId)?.text
	}

	fun selectionOf(intentId: String, mutationId: String): Boolean? = selectionByDraft[intentId to mutationId]

	// The intent the student can confirm now: open in the conversation and not already running.
	fun openIntent(intentId: String): Intent? {
		return conversation?.openIntents?.get(intentId)?.takeUnless { intentId in runningIntents }
	}

	// The send with [sentText] failed: its bubble is the student's last message, or the next one the conversation
	// shows when the store has not caught up yet.
	fun markSendFailed(sentText: String) {
		val last = lastUserMessage()

		if (last != null && last.text == sentText) {
			failedMessages += last.id
		} else {
			pendingFailedText = sentText
		}
	}

	fun observe(observed: AssistantConversation) {
		conversation = observed

		val pending = pendingFailedText
		val last = lastUserMessage()

		if (pending != null && last != null && last.text == pending) {
			failedMessages += last.id
			pendingFailedText = null
		}

		// A turn whose result is unknown is retried with the message it answers, under the id that message was sent with.
		if (observed.activeTurn?.status == AssistantTurnPhase.UNCONFIRMED && last != null) {
			lastFailedSend = FailedSend(text = last.text, suggestionId = followedSuggestionId, clientTurnId = last.id)
		}
	}

	internal fun lastUserMessage(): AssistantUserMessage? {
		return conversation?.items
			?.filterIsInstance<AssistantConversationItem.User>()
			?.lastOrNull()
			?.message
	}

	// What a retry sends again: the text, the suggestion it was picked from, and the client turn id it was sent with.
	internal data class FailedSend(val text: String, val suggestionId: String?, val clientTurnId: String)

	internal data class TurnFailure(val message: String, val retryable: Boolean)

	internal data class Quota(val retryAfterSeconds: Int?)

	// [level] and [canAppeal] are unknown until the standing is read, and the message says so.
	internal data class Restriction(val level: StandingLevel?, val canAppeal: Boolean)
}
