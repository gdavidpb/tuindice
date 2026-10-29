package com.gdavidpb.tuindice.summary.domain.mapper

import com.gdavidpb.tuindice.summary.testing.termReportInsight
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SummaryTermReportEntryTest {

	@Test
	fun theNewestReportNotOpenedIsTheLine() {
		val older = termReportInsight(id = "term-1:1", createdAtMillis = 100L)
		val newer = termReportInsight(id = "term-1:2", createdAtMillis = 300L)
		val middle = termReportInsight(id = "term-1:3", createdAtMillis = 200L)

		val entry = termReportEntryOf(insights = listOf(older, newer, middle), seenIds = emptySet())

		assertEquals("term-1:2", entry?.insightId)
	}

	@Test
	fun aReportAlreadyOpenedIsLeftOut() {
		val newer = termReportInsight(id = "term-1:2", createdAtMillis = 300L)
		val older = termReportInsight(id = "term-1:1", createdAtMillis = 100L)

		val entry = termReportEntryOf(insights = listOf(newer, older), seenIds = setOf("term-1:2"))

		assertNull(entry)
	}

	@Test
	fun openingTheNewestHidesTheLineEvenWhenAnOlderReportIsNotOpened() {
		val newer = termReportInsight(id = "term-1:2", createdAtMillis = 300L)
		val older = termReportInsight(id = "term-1:1", createdAtMillis = 100L)

		val entry = termReportEntryOf(insights = listOf(older, newer), seenIds = setOf("term-1:2"))

		assertNull(entry)
	}

	@Test
	fun noLineWhenEveryReportWasOpened() {
		val entry = termReportEntryOf(
			insights = listOf(termReportInsight(id = "term-1:1")),
			seenIds = setOf("term-1:1")
		)

		assertNull(entry)
	}

	@Test
	fun theLineNamesTheTermOfTheReportAndItsTitle() {
		val entry = termReportEntryOf(
			insights = listOf(termReportInsight(termKey = "2026-APR_JUL")),
			seenIds = emptySet()
		)

		assertEquals("Abr - Jul 2026", entry?.periodLabel)
		assertEquals("Cerraste con 4,18", entry?.subtitle)
	}

	@Test
	fun aTermKeyTheAppDoesNotKnowIsNamedAsItIs() {
		assertEquals("2026-XYZ", termLabelOf(termKey = "2026-XYZ"))
	}
}
