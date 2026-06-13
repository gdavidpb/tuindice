package com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.record.domain.exception.SyntheticTermValidationException
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecordExceptionHandlerTest {
	private val handler = RecordExceptionHandler()

	@Test
	fun parseException_mapsValidationException_keepingItsReason() {
		val error = handler.parseException(
			SyntheticTermValidationException(SyntheticTermValidationError.DUPLICATE_SUBJECT)
		)

		assertEquals(
			RecordUseCaseError.SyntheticTermValidation(
				SyntheticTermValidationError.DUPLICATE_SUBJECT
			),
			error
		)
	}

	@Test
	fun parseException_mapsUnauthorizedResponse() {
		val error = handler.parseException(
			clientRequestException(HttpStatusCode.Unauthorized)
		)

		assertEquals(RecordUseCaseError.Unauthorized, error)
	}

	@Test
	fun parseException_mapsServiceUnavailableResponse() {
		val error = handler.parseException(
			serverResponseException(HttpStatusCode.ServiceUnavailable)
		)

		assertEquals(RecordUseCaseError.Unavailable, error)
	}

	@Test
	fun parseException_mapsTimeoutMessages_evenWhenNested() {
		assertEquals(
			RecordUseCaseError.Timeout,
			handler.parseException(IllegalStateException("Request timed out"))
		)
		assertEquals(
			RecordUseCaseError.Timeout,
			handler.parseException(
				IllegalStateException("wrapper", RuntimeException("connection timeout"))
			)
		)
	}

	@Test
	fun parseException_mapsConnectionMessages_toNoConnection() {
		val error = handler.parseException(
			IllegalStateException("Could not connect to the server")
		)

		assertEquals(RecordUseCaseError.NoConnection, error)
	}

	@Test
	fun parseException_returnsNull_forUnmappedExceptions() {
		assertNull(handler.parseException(IllegalArgumentException("boom")))
	}
}
