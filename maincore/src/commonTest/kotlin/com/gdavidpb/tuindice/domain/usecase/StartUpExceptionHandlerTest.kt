package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StartUpExceptionHandlerTest {
	@Test
	fun startupExceptionHandler_returnsNoServices_forGooglePlayServicesErrors() {
		val throwable = GooglePlayServicesNotAvailableException()

		val actual = StartUpExceptionHandler().parseException(throwable)

		assertEquals(StartUpUseCaseError.NoServices, actual)
	}

	@Test
	fun startupExceptionHandler_returnsNull_forUnhandledExceptions() {
		val throwable = IllegalStateException("boom")

		val actual = StartUpExceptionHandler().parseException(throwable)

		assertNull(actual)
	}

	private class GooglePlayServicesNotAvailableException : RuntimeException()
}
