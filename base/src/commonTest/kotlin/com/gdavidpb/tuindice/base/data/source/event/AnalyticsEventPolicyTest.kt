package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnalyticsEventPolicyTest {
	@Test
	fun selfLoopTransitions_areNotAnalyticsRelevant() {
		val selfLoop = AppEvent.Transition(
			source = "sign_in",
			from = "idle",
			event = "SetPassword",
			to = "idle"
		)

		assertTrue(selfLoop.isSelfLoop)
		assertFalse(selfLoop.isAnalyticsRelevant())
	}

	@Test
	fun stateChangingTransitions_areAnalyticsRelevant() {
		val transition = AppEvent.Transition(
			source = "sign_in",
			from = "idle",
			event = "ClickSignIn",
			to = "logging_in"
		)

		assertFalse(transition.isSelfLoop)
		assertTrue(transition.isAnalyticsRelevant())
	}

	@Test
	fun invalidTransitions_areAnalyticsRelevant() {
		val invalid = AppEvent.InvalidTransition(
			source = "summary",
			from = "loading",
			event = "ObserveSummary"
		)

		assertTrue(invalid.isAnalyticsRelevant())
	}

	@Test
	fun everyOtherEvent_isAnalyticsRelevant() {
		assertTrue(AppEvent.ScreenView(source = "summary").isAnalyticsRelevant())
		assertTrue(
			AppEvent.Action(source = "summary", action = "RefreshSummary").isAnalyticsRelevant()
		)
	}
}
