package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSectionItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermItem
import com.gdavidpb.tuindice.summary.testing.summaryContentState
import com.gdavidpb.tuindice.summary.testing.summaryHubItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_link_record
import kotlin.test.Test
import kotlin.test.assertEquals

class SummaryAssistantContextTest {
	@Test
	fun withATermInProgress_contextNamesTheTermAndTheFiguresTheHubDraws() {
		val base = summaryHubItem()
		val hub = base.copy(
			term = base.term?.copy(termId = "term-2026-SEP_DEC"),
			statistics = SummaryStatisticsSectionItem.Figures(
				grade = 4.25f,
				line = listOf(4f, 4.25f),
				phrase = UiText.Empty
			)
		)

		val context = summaryContentState(hub = hub).assistantContext

		assertEquals(AssistantScreen.SUMMARY, context.screen)
		assertEquals(
			listOf(AssistantEntityRef(kind = AssistantEntityKind.TERM, id = "term-2026-SEP_DEC")),
			context.entities
		)
		assertEquals(
			mapOf(
				"currentTermLabel" to "Sep - Dic 2026",
				"attentionCount" to "2",
				"approvedCredits" to "156",
				"cumulativeIndex" to "4.2500",
				"week" to "3",
				"totalWeeks" to "12"
			),
			context.visibleFields
		)
		assertEquals(emptyMap<String, String>(), context.filters)
	}

	@Test
	fun withNoTermInProgress_contextNamesOnlyWhatTheHubHas() {
		val hub = summaryHubItem(
			attention = emptyList(),
			week = null,
			term = SummaryTermItem(
				linkText = UiText.Resource(Res.string.summary_link_record),
				week = null,
				summaryText = null,
				subjects = emptyList(),
				finished = null,
				card = null
			),
			career = null
		)

		val context = summaryContentState(hub = hub).assistantContext

		assertEquals(AssistantScreen.SUMMARY, context.screen)
		assertEquals(emptyList<AssistantEntityRef>(), context.entities)
		assertEquals(mapOf("attentionCount" to "0"), context.visibleFields)
	}

	@Test
	fun withATermWhoseRecordIdIsUnknown_contextNamesTheTermWithoutAnEntity() {
		val context = summaryContentState(hub = summaryHubItem()).assistantContext

		assertEquals(emptyList<AssistantEntityRef>(), context.entities)
		assertEquals("Sep - Dic 2026", context.visibleFields["currentTermLabel"])
	}

	@Test
	fun otherStates_describeTheScreenAlone() {
		val expected = AssistantScreenContext(screen = AssistantScreen.SUMMARY)

		assertEquals(expected, Summary.State.Idle().assistantContext)
		assertEquals(expected, Summary.State.Loading().assistantContext)
		assertEquals(expected, Summary.State.Failed().assistantContext)
	}
}
