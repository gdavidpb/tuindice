package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveTermReportUseCaseError
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_failed_message
import tuindice.summary.generated.resources.term_report_failed_session_message
import tuindice.summary.generated.resources.term_report_failed_title
import kotlin.test.Test
import kotlin.test.assertEquals

// The words of a report that cannot be read: a refused session says so, and a lost connection offers to try again.
class TermReportErrorMessagesTest {
	@Test
	fun when_theSessionEnded_then_theFailedSaysThatTheSessionIsNotActive() {
		val failed = termReportUnavailableState(ObserveTermReportUseCaseError.SessionEnded)

		assertEquals(UiText.Resource(Res.string.term_report_failed_title), failed.title)
		assertEquals(UiText.Resource(Res.string.term_report_failed_session_message), failed.message)
	}

	@Test
	fun when_theConnectionFailed_then_theFailedOffersToTryAgain() {
		val failed = termReportUnavailableState(ObserveTermReportUseCaseError.NoConnection)

		assertEquals(UiText.Resource(Res.string.term_report_failed_title), failed.title)
		assertEquals(UiText.Resource(Res.string.term_report_failed_message), failed.message)
	}

	@Test
	fun when_theCauseIsUnknown_then_theFailedOffersToTryAgain() {
		val failed = termReportUnavailableState(cause = null)

		assertEquals(UiText.Resource(Res.string.term_report_failed_message), failed.message)
	}
}
