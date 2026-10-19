package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.AssistantIntentOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind

internal fun OutcomeKind.toPresentation(): AssistantIntentOutcome {
	return when (this) {
		OutcomeKind.CONFIRMED -> AssistantIntentOutcome.CONFIRMED
		OutcomeKind.QUEUED -> AssistantIntentOutcome.QUEUED
		OutcomeKind.CONFLICT -> AssistantIntentOutcome.CONFLICT
		OutcomeKind.REJECTED -> AssistantIntentOutcome.REJECTED
		OutcomeKind.CANCELLED -> AssistantIntentOutcome.CANCELLED
		OutcomeKind.EXPIRED -> AssistantIntentOutcome.EXPIRED
		OutcomeKind.FAILED -> AssistantIntentOutcome.FAILED
		OutcomeKind.DELEGATED -> AssistantIntentOutcome.DELEGATED
	}
}
