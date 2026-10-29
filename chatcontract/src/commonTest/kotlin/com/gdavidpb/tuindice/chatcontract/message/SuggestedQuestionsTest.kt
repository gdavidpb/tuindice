package com.gdavidpb.tuindice.chatcontract.message

import com.gdavidpb.tuindice.chatcontract.context.ScreenId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SuggestedQuestionsTest {
	@Test
	fun everyScreenOffersThreeQuestionsAndNoScreenOffersTheGeneralOnes() {
		ScreenId.entries.forEach { screen -> assertEquals(3, SuggestedQuestions.forScreen(screen).size, "$screen") }
		assertEquals(SuggestedQuestions.forScreen(null).map { it.id }.toSet().size, 3)
	}

	@Test
	fun idsAreUniqueAndEveryOfferedQuestionIsFoundById() {
		assertEquals(SuggestedQuestions.all.size, SuggestedQuestions.all.map { it.id }.toSet().size)
		(ScreenId.entries.map { SuggestedQuestions.forScreen(it) } + listOf(SuggestedQuestions.forScreen(null))).flatten()
			.forEach { reply -> assertEquals(reply, SuggestedQuestions.byId(reply.id)) }
		assertTrue(SuggestedQuestions.all.all { it.id.startsWith("sq.") && it.text.isNotBlank() })
	}

	@Test
	fun anUnknownIdIsNotASuggestion() {
		assertNull(SuggestedQuestions.byId("sq.unknown"))
	}
}
