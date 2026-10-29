package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversationItem
import com.gdavidpb.tuindice.assistant.domain.model.AssistantTurnPhase
import com.gdavidpb.tuindice.assistant.domain.model.AssistantUserMessage
import com.gdavidpb.tuindice.assistant.domain.usecase.param.SendAssistantMessageParams
import com.gdavidpb.tuindice.assistant.presentation.mapper.lastAgentSuggestions
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.ui.model.EvaluationCardItem
import com.gdavidpb.tuindice.chatcontract.abuse.StandingLevel
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.memory.MemoryVocabulary
import com.gdavidpb.tuindice.chatcontract.message.SuggestedQuestions
import kotlinx.coroutines.Job

// The extended state of the chat sheet: what the table keeps besides its state. The conversation is the one the store
// observed; the rest is what the student did on the sheet and what the outcomes of their requests left. The table's
// rows write it one at a time. The commands that open the sheet write their part before the table takes their outcome,
// and nothing else writes it from outside the table, so it needs no lock.
internal class AssistantChatSession {
	var openedContext: AssistantScreenContext? = null
	var conversation: AssistantConversation? = null

	// The cards of the Evaluations tab for the evaluations the conversation's lists name, by id, read with each
	// observation of the conversation: a list draws the ones the device has with them.
	var evaluationCards: Map<String, EvaluationCardItem> = emptyMap()
	var guidedByMode = false
	var guidedByServer = false
	var lastFailedSend: FailedSend? = null
	var turnFailure: TurnFailure? = null

	// The notice of a photo that was not read. It is the photo's own: a text turn's failure is not a photo's, and the
	// other way round. It shows when no text turn has failed.
	var photoFailure: TurnFailure? = null
	var quota: Quota? = null
	var restriction: Restriction? = null
	var pendingFailedText: String? = null

	// What each accepted turn answers, as it was sent: its text, the suggestion it was picked from and its client turn id.
	// The retry of a turn sends that message again, whichever message the student wrote after it.
	val sentByTurn = mutableMapOf<String, FailedSend>()

	// The message waiting for the recovery of the unconfirmed answer before it (see sendNewMessage); null when none waits.
	var pendingSend: PendingSend? = null

	// The failure of the answer a new message waited for, when that answer ended with a RunError. Its notice keeps the
	// retry of that answer for as long as the last failed send is that answer's (see presentedNotice).
	var recoveredFailure: RecoveredFailure? = null

	// The unconfirmed turn the conversation showed at its last observation, if any: a turn is offered its retry when it
	// starts being unconfirmed, and it is not offered again on each observation while it stays so.
	var observedUnconfirmedTurnId: String? = null

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

	// The ids of the «¿Sigue aplicando?» cards a read already asked covers: a card without one asks for a new read. A
	// failed read keeps its cards covered, so the same cards do not ask again on the next observation.
	val memoryReadCards = mutableSetOf<String>()

	// The last read asked for a new card of each memory: the card waits for it, and its answer is the state that read
	// brings. A read that failed is in [failedReads]: the card stays open, with its buttons.
	val coveringReads = mutableMapOf<String, Int>()
	val failedReads = mutableSetOf<Int>()

	// The answers being saved, and the ones the service refused, by memory: a refused answer waits for the read asked
	// after its refusal, which says whether the memory still waits for it. The number is that read's.
	val pendingMemories = mutableSetOf<String>()
	val failedAnswers = mutableMapOf<String, Int>()

	// The intents whose confirmation runs, each with the job that runs it: an intent is marked while its job is active.
	val runningIntents = mutableMapOf<String, Job>()
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
		// A retry of an unconfirmed turn is over with its own first event or its end, not with the turn already shown.
		val shownTurn = observed.activeTurn?.takeIf { turn ->
			turn.turnId == sendingTurnId && turn.status != AssistantTurnPhase.UNCONFIRMED
		}

		if (sending && shownTurn != null) sending = false

		val pending = pendingFailedText
		val last = lastUserMessage()

		if (pending != null && last != null && last.text == pending) {
			failedMessages += last.id
			pendingFailedText = null
		}

		// A turn whose result is unknown is retried with the message it answers. The conversation is observed again while it
		// stays unconfirmed, as a message written meanwhile is stored: that does not offer the retry again, so a refusal
		// that came after keeps its own retry.
		val unconfirmedTurnId = observed.activeTurn?.takeIf { turn -> turn.status == AssistantTurnPhase.UNCONFIRMED }?.turnId

		if (unconfirmedTurnId != null && unconfirmedTurnId != observedUnconfirmedTurnId) {
			failedSendOf(unconfirmedTurnId)?.let { failed -> lastFailedSend = failed }
		}

		observedUnconfirmedTurnId = unconfirmedTurnId
	}

	// What a retry sends again: the text, the suggestion it was picked from, and the client turn id it was sent with. The
	// answer of a run that failed is a turn of its own: [runEnded] marks that failure, and its retry is a new request.
	internal data class FailedSend(
		val text: String,
		val suggestionId: String?,
		val clientTurnId: String,
		val runEnded: Boolean = false
	)

	// A message waiting for the recovery of the unconfirmed turn [turnId]. [restoreText] is what the composer
	// takes back when the message is not sent.
	internal data class PendingSend(
		val turnId: String,
		val params: SendAssistantMessageParams,
		val restoreText: String?
	)

	internal data class TurnFailure(val message: String, val retryable: Boolean)

	// The failure of the answer [clientTurnId] that ended with a RunError, when a new message waited for it: the
	// question of that answer is what its retry sends again.
	internal data class RecoveredFailure(val clientTurnId: String, val failure: TurnFailure)

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
// holds over the reads asked before it, unless a new card of the memory asked a read after it. A card whose read has
// not come back, or failed, still asks; otherwise the state of the memory in the list decides.
internal fun AssistantChatSession.memoryAnswer(memoryId: String): Boolean? {
	val saved = answeredMemories[memoryId]
	val cover = coveringReads[memoryId]
	val states = memoryStates

	return when {
		saved != null && memoryStatesRead <= saved.readsBefore && (cover == null || cover <= saved.readsBefore) ->
			saved.applies
		cover != null && cover > memoryStatesRead -> null
		states == null -> null
		states[memoryId] == null -> false
		states[memoryId] == MemoryVocabulary.STATE_ACTIVE -> true
		else -> null
	}
}

// A card of [memoryId] waits for the read that covers it: the read was asked, has not come back, and did not fail.
internal fun AssistantChatSession.awaitsMemoryRead(memoryId: String): Boolean {
	return coveringReads[memoryId]?.let { cover -> cover > memoryStatesRead && cover !in failedReads } == true
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

// The message the turn [turnId] answers, as it was sent. A turn this sheet did not send has no record: the last
// message the conversation has is the one it answers, as the turn was sent before the sheet was opened.
internal fun AssistantChatSession.failedSendOf(turnId: String): AssistantChatSession.FailedSend? {
	return sentByTurn[turnId] ?: lastUserMessage()?.let { last ->
		AssistantChatSession.FailedSend(text = last.text, suggestionId = null, clientTurnId = last.id)
	}
}

// The follow of the sent turn [turnId] ended without its final event: the result of the turn is unknown, as the store
// shows it, and the send that waited for it is over. Its retry is offered again, as an unconfirmed turn offers it,
// with the message the turn answers, as it was sent. A send that turnAccepted already answered is not waiting here.
internal fun AssistantChatSession.endSendUnconfirmed(turnId: String) {
	if (!sending || sendingTurnId != turnId) return

	sending = false

	sentByTurn[turnId]?.let { sent -> lastFailedSend = sent }
}

// A send waits while a message is on its way, a turn runs or a photo is read; a suggestion and a retry wait too.
internal fun AssistantChatSession.sendIsBusy(): Boolean = sending || isRunning() || photoExtracting
