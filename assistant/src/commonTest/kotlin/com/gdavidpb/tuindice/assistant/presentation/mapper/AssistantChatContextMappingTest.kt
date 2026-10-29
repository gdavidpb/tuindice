package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.testing.FakeAssistantChatTexts
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

// The chip of each screen, built from the fields that screen declares: its name and, at most, the one field that reads
// on its own. The fake texts name a screen as "screen:<NAME>".
class AssistantChatContextMappingTest {
	private val texts = FakeAssistantChatTexts()

	@Test
	fun theSummary_namesTheTermInProgress_andNoneOfItsFigures() = runTest {
		val context = AssistantScreenContext(
			screen = AssistantScreen.SUMMARY,
			entities = listOf(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-1")),
			visibleFields = mapOf(
				"currentTermLabel" to "Abr - Jul 2026",
				"attentionCount" to "1",
				"approvedCredits" to "152",
				"cumulativeIndex" to "4.1250",
				"week" to "3",
				"totalWeeks" to "12"
			)
		)

		assertEquals("screen:SUMMARY · Abr - Jul 2026", context.toChatContextItem(texts).label)
	}

	@Test
	fun theSummaryWithoutATermInProgress_isNamedAlone() = runTest {
		val context = AssistantScreenContext(
			screen = AssistantScreen.SUMMARY,
			visibleFields = mapOf("attentionCount" to "0", "cumulativeIndex" to "4.1250")
		)

		assertEquals("screen:SUMMARY", context.toChatContextItem(texts).label)
	}

	@Test
	fun theRecord_namesTheTermOnScreen() = runTest {
		val context = AssistantScreenContext(
			screen = AssistantScreen.RECORD,
			visibleFields = mapOf(
				"termLabel" to "Jul - Ago 2026",
				"termIndex" to "5.0000",
				"cumulativeIndex" to "4.1314",
				"credits" to "7",
				"viewMode" to "projection",
				"isSynthetic" to "true"
			)
		)

		assertEquals("screen:RECORD · Jul - Ago 2026", context.toChatContextItem(texts).label)
	}

	@Test
	fun aSubjectOfTheRecord_namesTheSubject_overTheTerm() = runTest {
		val context = AssistantScreenContext(
			screen = AssistantScreen.RECORD,
			entities = listOf(
				AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = "attempt-1"),
				AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "EP1308")
			),
			visibleFields = mapOf(
				"subjectCode" to "EP1308",
				"subjectName" to "Proyecto de grado I",
				"termLabel" to "Jul - Ago 2026",
				"credits" to "3"
			)
		)

		assertEquals("screen:RECORD · EP1308 Proyecto de grado I", context.toChatContextItem(texts).label)
	}

	@Test
	fun aSubjectOfThePensum_namesItsCodeAndName() = runTest {
		val context = AssistantScreenContext(
			screen = AssistantScreen.SUBJECT_DETAIL,
			entities = listOf(AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "MA1111")),
			visibleFields = mapOf(
				"displayCode" to "MA1111",
				"name" to "Matemáticas I",
				"credits" to "4",
				"status" to "APPROVED"
			)
		)

		assertEquals("screen:SUBJECT_DETAIL · MA1111 Matemáticas I", context.toChatContextItem(texts).label)
	}

	@Test
	fun anEvaluation_namesItselfWithItsSubject() = runTest {
		val context = AssistantScreenContext(
			screen = AssistantScreen.EVALUATION_DETAIL,
			entities = listOf(
				AssistantEntityRef(kind = AssistantEntityKind.EVALUATION, id = "evaluation-1"),
				AssistantEntityRef(kind = AssistantEntityKind.SUBJECT, id = "MA1111"),
				AssistantEntityRef(kind = AssistantEntityKind.ATTEMPT, id = "attempt-1")
			),
			visibleFields = mapOf(
				"title" to "PARCIAL 1",
				"date" to "2026-10-20",
				"points" to "15 / 20",
				"grade" to "15.00",
				"maxGrade" to "20.00",
				"state" to "Completada"
			)
		)

		assertEquals("screen:EVALUATION_DETAIL · MA1111 PARCIAL 1", context.toChatContextItem(texts).label)
	}

	@Test
	fun anEvaluationWithoutASubject_namesItselfAlone() = runTest {
		val context = AssistantScreenContext(
			screen = AssistantScreen.EVALUATION_DETAIL,
			visibleFields = mapOf("title" to "PARCIAL 1", "grade" to "15.00")
		)

		assertEquals("screen:EVALUATION_DETAIL · PARCIAL 1", context.toChatContextItem(texts).label)
	}

	@Test
	fun screensThatDeclareOnlyFigures_areNamedAlone() = runTest {
		val contexts = listOf(
			AssistantScreenContext(
				screen = AssistantScreen.PENSUM,
				visibleFields = mapOf("approvedCredits" to "152", "totalCredits" to "240")
			),
			AssistantScreenContext(
				screen = AssistantScreen.STATISTICS,
				visibleFields = mapOf("approvedSubjects" to "40", "failedSubjects" to "2", "approvedCredits" to "152")
			),
			AssistantScreenContext(
				screen = AssistantScreen.EVALUATIONS,
				filters = mapOf("week" to "2026-09-28:3")
			),
			AssistantScreenContext(screen = AssistantScreen.SUBJECT_SEARCH),
			AssistantScreenContext(screen = AssistantScreen.ABOUT)
		)

		contexts.forEach { context ->
			assertEquals("screen:${context.screen.name}", context.toChatContextItem(texts).label)
		}
	}

	@Test
	fun theChip_keepsTheWholeContext_thatTheTurnSends() = runTest {
		val context = AssistantScreenContext(
			screen = AssistantScreen.SUMMARY,
			visibleFields = mapOf("currentTermLabel" to "Abr - Jul 2026", "cumulativeIndex" to "4.1250")
		)

		assertSame(context, context.toChatContextItem(texts).context)
	}

	@Test
	fun theTermReport_namesItsTermLabel() = runTest {
		val context = AssistantScreenContext(
			screen = AssistantScreen.TERM_REPORT,
			entities = listOf(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-1")),
			visibleFields = mapOf("termLabel" to "Abr - Jul 2026")
		)

		assertEquals("screen:TERM_REPORT · Abr - Jul 2026", context.toChatContextItem(texts).label)
	}
}
