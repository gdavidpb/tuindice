package com.gdavidpb.tuindice.summary.data.source

import com.gdavidpb.tuindice.summary.testing.FakeSettings
import com.gdavidpb.tuindice.summary.utils.PreferencesKeys
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TermReportFeedbackDataSourceTest {
	@Test
	fun when_aVoteOnATermReportIsConfirmed_then_itIsKeptAndReadBackByANewSource() = runTest {
		val settings = FakeSettings()
		val dataSource = TermReportFeedbackDataSource(settings)

		dataSource.saveTermReportFeedback(insightId = "term-1:2", useful = true)
		dataSource.saveTermReportFeedback(insightId = "term-1:3", useful = false)
		dataSource.saveTermReportFeedback(insightId = "term-1:2", useful = false)

		val reopened = TermReportFeedbackDataSource(settings)

		assertEquals(
			mapOf("term-1:2" to false, "term-1:3" to false),
			reopened.observeTermReportFeedbacks().first()
		)
	}

	@Test
	fun when_theSessionIsCleared_then_theVotesOfTheTermReportsAreGone() = runTest {
		val settings = FakeSettings()
		val dataSource = TermReportFeedbackDataSource(settings)
		dataSource.saveTermReportFeedback(insightId = "term-1:2", useful = true)

		settings.remove(PreferencesKeys.TERM_REPORT_FEEDBACKS)
		dataSource.clearSessionMemory()

		assertEquals(emptyMap<String, Boolean>(), dataSource.observeTermReportFeedbacks().first())
	}
}
