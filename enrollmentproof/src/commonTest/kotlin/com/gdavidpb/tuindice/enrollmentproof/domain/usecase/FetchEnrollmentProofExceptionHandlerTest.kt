package com.gdavidpb.tuindice.enrollmentproof.domain.usecase

import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FetchEnrollmentProofExceptionHandlerTest {
	@Test
	fun fetchEnrollmentProofExceptionHandler_mapsUnsupportedFiles() {
		val throwable = UnsupportedOperationException("Unsupported file")

		val actual = FetchEnrollmentProofExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(FetchEnrollmentProofUseCaseError.UnsupportedFile, actual)
	}

	@Test
	fun fetchEnrollmentProofExceptionHandler_mapsNotFoundStatus() {
		val throwable = clientRequestException(HttpStatusCode.NotFound, path = "/enrollment-proof/v1")

		val actual = FetchEnrollmentProofExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(FetchEnrollmentProofUseCaseError.NotFound, actual)
	}

	@Test
	fun fetchEnrollmentProofExceptionHandler_mapsConnectionToNoConnection() {
		val throwable = IllegalStateException("network is unreachable")

		val actual = FetchEnrollmentProofExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = false)
		).parseException(throwable)

		val error = assertIs<FetchEnrollmentProofUseCaseError.NoConnection>(actual)
		assertEquals(false, error.isNetworkAvailable)
	}
}
