package com.gdavidpb.tuindice.assistant.domain.repository

import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome

/**
 * Implemented by maincore. The mutations of an intent that were still running when the intent answered QUEUED: their
 * outcomes are followed past that answer, for as long as the app session lasts, not only while the chat is open.
 */
interface AssistantQueuedMutationsRepository {
	/**
	 * Hands [onSettled] the final outcome of each of the queued mutations [mutationIds] that settles before [untilMillis],
	 * once they all have one or that instant passes, and stops following them. A mutation nothing queued, or one that does
	 * not settle in time, is left out; when none settles, [onSettled] is not called.
	 */
	fun followQueued(mutationIds: List<String>, untilMillis: Long, onSettled: suspend (List<MutationOutcome>) -> Unit)
}
