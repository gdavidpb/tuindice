package com.gdavidpb.tuindice.assistant.domain.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantSuggestionFacts
import com.gdavidpb.tuindice.chatcontract.context.ScreenContext
import com.gdavidpb.tuindice.chatcontract.context.ScreenId
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantScreenContextMapperTest {
	// The locale and the time zone the app sends are the contract's defaults: a change on either side fails here.
	@Test
	fun theContextIsSentWithTheLocaleAndTheTimeZoneOfTheContract() {
		val contract = ScreenContext(screen = ScreenId.SUMMARY)

		val sent = AssistantScreenContext(screen = AssistantScreen.SUMMARY).toScreenContext()

		assertEquals(contract.locale, sent.locale)
		assertEquals(contract.timeZone, sent.timeZone)
	}

	@Test
	fun localSuggestionFactsAreNotSentToTheService() {
		val context = AssistantScreenContext(
			screen = AssistantScreen.SUMMARY,
			suggestionFacts = AssistantSuggestionFacts(hasCurrentTerm = false, hasCreditPace = true)
		)

		assertEquals(ScreenContext(screen = ScreenId.SUMMARY), context.toScreenContext())
	}
}
