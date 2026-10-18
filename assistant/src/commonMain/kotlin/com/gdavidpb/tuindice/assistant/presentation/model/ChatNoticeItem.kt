package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// What limits the chat right now, shown above the composer. Texts are ready to show, but a restriction's, whose dates
// are read where the text is drawn, as the app's dates are.
sealed interface ChatNoticeItem {
	// The last turn failed; [canRetry] shows the retry action.
	data class TurnFailed(val message: String, val canRetry: Boolean) : ChatNoticeItem

	// The usage limit was reached until [message] says.
	data class QuotaExhausted(val message: String) : ChatNoticeItem

	// A pause, a suspension or a ban, and until when. [canAppeal] shows the appeal action (≤ 500 characters).
	data class Restricted(val message: UiText, val canAppeal: Boolean) : ChatNoticeItem

	// Guided mode: open chat is paused and only the suggestions work.
	data class Guided(val message: String) : ChatNoticeItem
}
