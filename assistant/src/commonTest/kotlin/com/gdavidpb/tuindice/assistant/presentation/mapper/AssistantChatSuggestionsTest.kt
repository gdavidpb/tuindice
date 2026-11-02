package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantSuggestionFacts
import com.gdavidpb.tuindice.chatcontract.message.SuggestedQuestions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssistantChatSuggestionsTest {
	@Test
	fun summary_usesOnlyTheCurrentTermAttentionAndPaceThatAreKnown() {
		val context = AssistantScreenContext(
			screen = AssistantScreen.SUMMARY,
			suggestionFacts = AssistantSuggestionFacts(hasCurrentTerm = true, hasVisibleAttention = false, hasCreditPace = true)
		)

		assertEquals(listOf("sq.summary.term", "sq.summary.pace"), ids(context))
		assertEquals(listOf("sq.about.what"), ids(context.copy(suggestionFacts = AssistantSuggestionFacts())))
	}

	@Test
	fun evaluations_offerThisWeekOnlyWhenTheSelectedWeekIsCurrent() {
		val context = AssistantScreenContext(
			screen = AssistantScreen.EVALUATIONS,
			suggestionFacts = AssistantSuggestionFacts(selectedWeekIsCurrent = true, hasEvaluations = true)
		)

		assertEquals(listOf("sq.evaluations.week", "sq.evaluations.heavy", "sq.evaluations.points"), ids(context))
		assertEquals(
			listOf("sq.evaluations.heavy", "sq.evaluations.points"),
			ids(context.copy(suggestionFacts = context.suggestionFacts.copy(selectedWeekIsCurrent = false)))
		)
		assertEquals(
			listOf("sq.about.what"),
			ids(context.copy(suggestionFacts = AssistantSuggestionFacts(hasEvaluations = false)))
		)
	}

	@Test
	fun record_usesTheSelectedTermAndAvoidsInputDependentQuestionsWhenGuided() {
		val context = AssistantScreenContext(
			screen = AssistantScreen.RECORD,
			entities = listOf(AssistantEntityRef(AssistantEntityKind.TERM, "current")),
			suggestionFacts = AssistantSuggestionFacts(selectedTermIsCurrent = true, hasIndexHistory = true)
		)

		assertEquals(listOf("sq.summary.term", "sq.record.simulate", "sq.statistics.history"), ids(context))
		assertEquals(listOf("sq.summary.term", "sq.statistics.history"), ids(context, guided = true))
		val oldTerm = context.copy(
			suggestionFacts = AssistantSuggestionFacts(selectedTermIsCurrent = false, selectedTermIsLatestClosed = true)
		)
		assertEquals(listOf("sq.record.last", "sq.record.goal"), ids(oldTerm))
		assertEquals(listOf("sq.record.last"), ids(oldTerm, guided = true))
	}

	@Test
	fun anOlderHistoricalTermDoesNotPretendToBeTheCurrentOrLastOne() {
		val context = AssistantScreenContext(
			screen = AssistantScreen.RECORD,
			entities = listOf(AssistantEntityRef(AssistantEntityKind.TERM, "older")),
			suggestionFacts = AssistantSuggestionFacts(
				selectedTermIsCurrent = false,
				selectedTermIsLatestClosed = false,
				hasIndexHistory = true,
				hasBestTerm = true
			)
		)

		assertEquals(listOf("sq.statistics.history", "sq.statistics.best"), ids(context, guided = true))
	}

	@Test
	fun aSelectedSubjectUsesSubjectQuestions_butAPensumSlotIsNotARealSubject() {
		val selected = AssistantScreenContext(
			screen = AssistantScreen.RECORD,
			entities = listOf(
				AssistantEntityRef(AssistantEntityKind.ATTEMPT, "a1"),
				AssistantEntityRef(AssistantEntityKind.SUBJECT, "CI4325")
			)
		)
		val blankSubject = AssistantScreenContext(
			screen = AssistantScreen.SUBJECT_DETAIL,
			entities = listOf(AssistantEntityRef(AssistantEntityKind.SUBJECT, " "))
		)

		assertEquals(listOf("sq.subject.difficulty", "sq.subject.requirements", "sq.subject.unlocks"), ids(selected))
		assertEquals(listOf("sq.pensum.progress"), ids(AssistantScreenContext(AssistantScreen.SUBJECT_DETAIL)))
		assertEquals(listOf("sq.pensum.progress"), ids(blankSubject))
	}

	@Test
	fun pensumDoesNotOfferAGlobalRankingFromASingleSubjectTool() {
		val context = AssistantScreenContext(
			screen = AssistantScreen.PENSUM,
			entities = listOf(AssistantEntityRef(AssistantEntityKind.PENSUM, "p1")),
			suggestionFacts = AssistantSuggestionFacts(hasAvailableSubjects = true)
		)

		assertEquals(listOf("sq.pensum.progress", "sq.pensum.available"), ids(context))
		assertFalse("sq.pensum.critical" in ids(context, guided = true))
	}

	@Test
	fun everyPresentedQuestionRetainsTheSharedCatalogText() {
		val contexts = AssistantScreen.entries.map { screen -> AssistantScreenContext(screen = screen) } + listOf(null)
		contexts.forEach { context ->
			listOf(false, true).forEach { guided ->
				val suggestions = contextualSuggestions(context, guided)
				assertTrue(suggestions.isNotEmpty())
				suggestions.forEach { reply -> assertEquals(SuggestedQuestions.byId(reply.id), reply) }
			}
		}
	}

	private fun ids(context: AssistantScreenContext?, guided: Boolean = false): List<String> =
		contextualSuggestions(context, guided).map { it.id }
}
