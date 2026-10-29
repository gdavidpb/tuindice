package com.gdavidpb.tuindice.assistant.data.model

import com.gdavidpb.tuindice.chatcontract.event.AssistantEvent

// What the stream of a turn delivers to the app: an event of the chat contract, as the service sent it, or one of the
// app's own. The app's own events come from a payload this build cannot read in full; they read only what its envelope
// names, so the domain never sees them.
sealed interface AssistantStreamEvent {
	val seq: Long
	val turnId: String

	data class Contract(val event: AssistantEvent) : AssistantStreamEvent {
		override val seq: Long get() = event.seq
		override val turnId: String get() = event.turnId
	}

	// The events the app writes into the conversation itself. They follow the seq of their turn like the contract's do.
	sealed interface Own : AssistantStreamEvent {
		// An intent proposal whose intent this build cannot read. No card opens, because the intent can be shown neither
		// read nor confirmed here; the message that carries it shows the notice of a part it cannot show.
		data class UnknownIntentProposal(
			override val seq: Long,
			override val turnId: String,
			val messageId: String
		) : Own

		// An intent resolved with an outcome this build does not know, or with none. Its card loses its buttons and shows no
		// outcome label, since no outcome is known to show; its message shows the notice of a part it cannot show.
		data class ResolvedWithUnknownOutcome(
			override val seq: Long,
			override val turnId: String,
			val intentId: String
		) : Own
	}
}
