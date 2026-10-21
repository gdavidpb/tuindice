package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.presentation.mapper.lastAgentSuggestions
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.memory.MemoryVocabulary
import com.gdavidpb.tuindice.chatcontract.message.SuggestedQuestions

// The extended state of the chat sheet: what the table keeps besides its state. The conversation is the one the store
// observed; the rest is what the student did on the sheet and what the outcomes of their requests left. The table's
// rows write it one at a time. The commands that open the sheet write their part before the table takes their outcome,
// and nothing else writes it from outside the table, so it needs no lock.
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

	// A send is on its way from the moment it starts until the first event of its turn, or its failure. The turn it was
	// accepted as is known from its acceptance, so a stop can reach it before its first event.
	var sending = false
	var sendingTurnId: String? = null
	var photoExtracting = false

	// The notification card is on this sheet: it shows while the chat is empty and no message is being sent.
	var notificationOfferShown = false
	val usefulByMessage = mutableMapOf<String, Boolean>()

	// The messages whose report the service saved. The service keeps one report per message, so a reported message offers
	// no reasons again. Only a saved report is written here: a refused one leaves the message as it was.
	val reportedMessages = mutableSetOf<String>()
	val selectionByDraft = mutableMapOf<Pair<String, String>, Boolean>()

	// The answers to «¿Sigue aplicando?» the service saved on this sheet, by memory. [MemoryAnswer.readsBefore] is the
	// number of the last read asked when the answer was saved: a read asked after it decides the answer again.
	val answeredMemories = mutableMapOf<String, MemoryAnswer>()

	// The state of each memory the service lists, by memory id; null until a list is read. A memory the list does not
	// have is archived or deleted. [memoryStatesRead] is the number of the read that is applied; the reads are numbered
	// as they are asked, and a read that comes back late does not overwrite a newer one.
	var memoryStates: Map<String, String>? = null
	var memoryStatesRead = 0
	var memoryReadsRequested = 0

	// The ids of the «¿Sigue aplicando?» cards a read already asked covers: a card without one asks for a new read.
	val memoryReadCards = mutableSetOf<String>()

	// The answers being saved, and the ones the service refused, by memory: a refused answer waits for the read asked
	// after its refusal, which says whether the memory still waits for it. The number is that read's.
	val pendingMemories = mutableSetOf<String>()
	val failedAnswers = mutableMapOf<String, Int>()
	val runningIntents = mutableSetOf<String>()
	val failedMessages = mutableSetOf<String>()

	// The restriction that holds at [nowMillis]: one whose end has passed no longer stops the chat, and the next send
	// reads the standing again.
	fun restrictionAt(nowMillis: Long): Restriction? = restriction?.takeUnless { held -> held.isOver(nowMillis) }

	fun isRunning(): Boolean = conversation?.activeTurn?.status == AssistantTurnPhase.RUNNING

	// The turn a stop would cancel: the one that runs, or the one being sent. Null when none can be stopped.
	fun stopTarget(): String? = if (isRunning()) conversation?.activeTurn?.turnId else sendingTurnId?.takeIf { sending }

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

	// The thumbs of [messageId] go back to [previous] when the service refused [rejected], unless the student has rated
	// the message again since: the newer rating is the one they meant.
	fun rateRefused(messageId: String, rejected: Boolean, previous: Boolean?) {
		if (usefulByMessage[messageId] != rejected) return

		if (previous == null) usefulByMessage.remove(messageId) else usefulByMessage[messageId] = previous
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

		// The first event of the sent turn, or its end, is what the conversation shows of it: the send is over then.
		if (sending && sendingTurnId != null && observed.activeTurn?.turnId == sendingTurnId) sending = false

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

	// What a retry sends again: the text, the suggestion it was picked from, and the client turn id it was sent with.
	internal data class FailedSend(val text: String, val suggestionId: String?, val clientTurnId: String)

	internal data class TurnFailure(val message: String, val retryable: Boolean)

	// A saved answer to a «¿Sigue aplicando?» question, and the number of the last read asked when it was saved.
	internal data class MemoryAnswer(val applies: Boolean, val readsBefore: Int)

	internal data class Quota(val retryAfterSeconds: Int?)

	// [level], its end [untilMillis] and [canAppeal] are unknown until the standing is read, and the message says so. A
	// restriction without an end holds until the server says otherwise.
	internal data class Restriction(val level: StandingLevel?, val untilMillis: Long?, val canAppeal: Boolean) {
		fun isOver(nowMillis: Long): Boolean = untilMillis != null && nowMillis >= untilMillis
	}
}

// The answer a «¿Sigue aplicando?» card of [memoryId] shows, null while it still asks. The answer saved on this sheet
// holds until a read asked after it; then the state of the memory in the list decides.
internal fun AssistantChatSession.memoryAnswer(memoryId: String): Boolean? {
	val saved = answeredMemories[memoryId]
	val states = memoryStates

	return when {
		saved != null && memoryStatesRead <= saved.readsBefore -> saved.applies
		states == null -> null
		states[memoryId] == null -> false
		states[memoryId] == MemoryVocabulary.STATE_ACTIVE -> true
		else -> null
	}
}

// The number of a new read of the list, asked now.
internal fun AssistantChatSession.nextMemoryRead(): Int {
	memoryReadsRequested++

	return memoryReadsRequested
}

// The last message the student wrote in the conversation the session observed, if any.
internal fun AssistantChatSession.lastUserMessage(): AssistantUserMessage? {
	return conversation?.items
		?.filterIsInstance<AssistantConversationItem.User>()
		?.lastOrNull()
		?.message
}
