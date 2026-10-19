package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.summary.domain.usecase.error.ObserveTermReportUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.RateTermReportUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateTermReportsUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.ObserveTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RateTermReportExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateTermReportsExceptionHandler
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TermReportExceptionHandlerTest {
	@Test
	fun rateExceptionHandler_mapsARefusalOfTheFeedback() {
		val unauthorized = clientRequestException(HttpStatusCode.Unauthorized, path = "/insights/v1/feedback")
		val notFound = clientRequestException(HttpStatusCode.NotFound, path = "/insights/v1/feedback")

		assertEquals(RateTermReportUseCaseError.Refused, RateTermReportExceptionHandler().parseException(unauthorized))
		assertEquals(RateTermReportUseCaseError.Refused, RateTermReportExceptionHandler().parseException(notFound))
	}

	@Test
	fun rateExceptionHandler_mapsConnectionToNoConnection_andLeavesOtherFailuresUnhandled() {
		assertEquals(
			RateTermReportUseCaseError.NoConnection,
			RateTermReportExceptionHandler().parseException(IllegalStateException("network is unreachable"))
		)
		assertNull(
			RateTermReportExceptionHandler().parseException(
				clientRequestException(HttpStatusCode.Conflict, path = "/insights/v1/feedback")
			)
		)
	}

	@Test
	fun observeExceptionHandler_mapsConnectionToNoConnection() {
		assertEquals(
			ObserveTermReportUseCaseError.NoConnection,
			ObserveTermReportExceptionHandler().parseException(IllegalStateException("network is unreachable"))
		)
		assertNull(ObserveTermReportExceptionHandler().parseException(IllegalStateException("record")))
	}

	@Test
	fun updateExceptionHandler_mapsConnectionToNoConnection() {
		assertEquals(
			UpdateTermReportsUseCaseError.NoConnection,
			UpdateTermReportsExceptionHandler().parseException(IllegalStateException("network is unreachable"))
		)
	}
}
