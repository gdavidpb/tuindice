package com.gdavidpb.tuindice.assistant.presentation.model

// What limits the chat right now, shown above the composer. Texts are ready to show.
sealed interface ChatNoticeItem {
	// The last turn failed; [canRetry] shows the retry action.
	data class TurnFailed(val message: String, val canRetry: Boolean) : ChatNoticeItem

	// The usage limit was reached until [message] says.
	data class QuotaExhausted(val message: String) : ChatNoticeItem

	// A warning, cooldown, suspension or ban. [canAppeal] shows the appeal action (≤ 500 characters).
	data class Restricted(val message: String, val canAppeal: Boolean) : ChatNoticeItem

	// Guided mode: open chat is paused and only the suggestions work.
	data class Guided(val message: String) : ChatNoticeItem
}
