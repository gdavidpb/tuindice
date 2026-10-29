package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSectionItem
import com.gdavidpb.tuindice.summary.testing.termReportItemFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TermReportAssistantContextTest {
	@Test
	fun theContextNamesTheReportItIsAbout_andWhatItSays() {
		val context = termReportItemFixture(insightId = "term-1:2").assistantContext

		assertEquals(AssistantScreen.TERM_REPORT, context.screen)
		assertEquals("term-1:2", context.visibleFields["reportId"])
		assertEquals("Cerraste con 4,18", context.visibleFields["reportTitle"])
		assertEquals(
			"Cerraste con 4,18\nAprobaste las 3 materias.\n\nTu ritmo\nEste trimestre aprobaste 11 créditos.",
			context.visibleFields["reportText"]
		)
		assertEquals("Abr - Jul 2026", context.visibleFields["termLabel"])
	}

	@Test
	fun eachSectionIsCutToItsOwnLimit() {
		val longText = "a".repeat(1_000)

		val text = reportTextOf(sections = listOf(TermReportSectionItem(title = "Tu ritmo", text = longText)))

		assertEquals(600, text.length)
		assertTrue(text.startsWith("Tu ritmo\naaa"))
	}

	@Test
	fun theWholeTextIsCutToTheTotalLimit() {
		val sections = List(10) { index ->
			TermReportSectionItem(title = "Sección $index", text = "b".repeat(500))
		}

		val text = reportTextOf(sections = sections)

		assertEquals(2_500, text.length)
	}
}
