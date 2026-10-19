package com.gdavidpb.tuindice.base.data.source

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InMemoryTermReportFocusDataSourceTest {

	@Test
	fun aRequestIsPendingUntilItsOwnReportIsTaken() = runTest {
		val focus = InMemoryTermReportFocusDataSource()

		focus.request(insightId = "term-1:2")
		focus.consume(insightId = "term-1:1")

		assertEquals("term-1:2", focus.observeRequest().first())

		focus.consume(insightId = "term-1:2")

		assertNull(focus.observeRequest().first())
	}

	@Test
	fun aNewerRequestReplacesOneNotTakenYet() = runTest {
		val focus = InMemoryTermReportFocusDataSource()

		focus.request(insightId = "term-1:1")
		focus.request(insightId = "term-1:2")

		assertEquals("term-1:2", focus.observeRequest().first())
	}

	@Test
	fun aRequestDoesNotOutliveTheSession() = runTest {
		val focus = InMemoryTermReportFocusDataSource()
		focus.request(insightId = "term-1:2")

		focus.clearSessionMemory()

		assertNull(focus.observeRequest().first())
	}
}
