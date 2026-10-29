package com.gdavidpb.tuindice.assistant.ui.style

import com.gdavidpb.tuindice.assistant.presentation.model.ChatPartItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AssistantChatMotionTest {
	@Test
	fun when_aTextIsShort_then_eachWordTakes30Ms() {
		assertEquals(30f, revealStepMillis(wordCount = 5))
	}

	@Test
	fun when_aTextIsLong_then_theWholeRevealStaysWithinTheCap() {
		listOf(40, 100, 500, 5_000).forEach { words ->
			assertTrue(
				revealTotalMillis(words) <= AssistantChatMotion.RevealMaxMillis + 0.5f,
				"$words words reveal in ${revealTotalMillis(words)} ms"
			)
		}
	}

	@Test
	fun when_aTextHasExtraWhitespace_then_wordsAreTheRunsOfNonWhitespace() {
		assertEquals(listOf(1..2, 5..7), wordRanges(" ab  cde "))
	}

	@Test
	fun when_anAnswerHasTwoTexts_then_theNextPartAndTheActionsFollowInOrder() {
		val plan = planAgentEntrance(listOf(ChatPartItem.Text("uno dos tres cuatro"), ChatPartItem.Text("cinco")))

		assertEquals(0, plan.partDelays[0])
		assertTrue(plan.partDelays[1] > plan.partDelays[0])
		// One word: its reveal is only its own fade, which the next part overlaps, so only the stagger remains.
		assertEquals(plan.partDelays[1] + AssistantChatMotion.StaggerMillis, plan.actionsDelay)
	}
}
