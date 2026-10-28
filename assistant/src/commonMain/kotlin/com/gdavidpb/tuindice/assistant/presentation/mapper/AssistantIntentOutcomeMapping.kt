package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.domain.model.DETAIL_PENSUM_NOT_LOADED
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantIntentOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind

// The card's outcome for a wire outcome and the detail it came with. Only a refusal reads its detail: the one the
// pensum explains gets its own label, and every other refusal keeps the label it always had.
internal fun OutcomeKind.toPresentation(detailCode: String?): AssistantIntentOutcome {
	return when (this) {
		OutcomeKind.CONFIRMED -> AssistantIntentOutcome.CONFIRMED
		OutcomeKind.QUEUED -> AssistantIntentOutcome.QUEUED
		OutcomeKind.CONFLICT -> AssistantIntentOutcome.CONFLICT
		OutcomeKind.REJECTED -> rejectedOutcome(detailCode)
		OutcomeKind.CANCELLED -> AssistantIntentOutcome.CANCELLED
		OutcomeKind.EXPIRED -> AssistantIntentOutcome.EXPIRED
		OutcomeKind.FAILED -> AssistantIntentOutcome.FAILED
		OutcomeKind.DELEGATED -> AssistantIntentOutcome.DELEGATED
	}
}

private fun rejectedOutcome(detailCode: String?): AssistantIntentOutcome {
	return if (detailCode == DETAIL_PENSUM_NOT_LOADED) {
		AssistantIntentOutcome.PENSUM_NOT_LOADED
	} else {
		AssistantIntentOutcome.REJECTED
	}
}
