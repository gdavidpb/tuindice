package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.domain.model.DETAIL_PENSUM_NOT_LOADED
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantIntentOutcome
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantIntentOutcomeMappingTest {
	@Test
	fun aRefusalForAPensumNotLoaded_isShownAsPensumNotLoaded() {
		assertEquals(
			AssistantIntentOutcome.PENSUM_NOT_LOADED,
			OutcomeKind.REJECTED.toPresentation(DETAIL_PENSUM_NOT_LOADED)
		)
	}

	@Test
	fun everyOtherRefusal_keepsTheRejectedLabel() {
		assertEquals(AssistantIntentOutcome.REJECTED, OutcomeKind.REJECTED.toPresentation("unknown_subject"))
		assertEquals(AssistantIntentOutcome.REJECTED, OutcomeKind.REJECTED.toPresentation(null))
	}

	@Test
	fun theDetailOfAnotherOutcome_changesNothing() {
		assertEquals(AssistantIntentOutcome.FAILED, OutcomeKind.FAILED.toPresentation(DETAIL_PENSUM_NOT_LOADED))
	}
}
