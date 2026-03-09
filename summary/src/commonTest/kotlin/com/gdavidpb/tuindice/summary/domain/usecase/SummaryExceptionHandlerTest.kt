package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.summary.domain.exception.ProfilePictureIllegalArgumentException
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetUserUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryExceptionHandlerTest {
	@Test
	fun getUserExceptionHandler_mapsConflictToOutdatedPassword() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = clientRequestException(HttpStatusCode.Conflict, path = "/users")

		val actual = GetUserExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true),
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(GetUserUseCaseError.OutdatedPassword, actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "GetUserExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	@Test
	fun uploadProfilePictureExceptionHandler_mapsValidationErrors() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = ProfilePictureIllegalArgumentException(ProfilePictureUseCaseError.InvalidSource)

		val actual = UploadProfilePictureExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true),
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(ProfilePictureUseCaseError.InvalidSource, actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "UploadProfilePictureExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	@Test
	fun removeProfilePictureExceptionHandler_mapsConnectionToNoConnection() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = IllegalStateException("network is unreachable")

		val actual = RemoveProfilePictureExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = false),
			reportingRepository = reportingRepository
		).reportException(throwable)

		val error = assertIs<ProfilePictureUseCaseError.NoConnection>(actual)
		assertEquals(false, error.isNetworkAvailable)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "RemoveProfilePictureExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	private fun assertReported(
		reportingRepository: RecordingReportingRepository,
		handlerName: String,
		throwable: Throwable,
		isHandled: Boolean
	) {
		assertEquals(listOf(throwable), reportingRepository.loggedExceptions)
		assertEquals(handlerName, reportingRepository.customKeys["useCase"])
		assertEquals(isHandled, reportingRepository.customKeys["isHandled"])
	}
}
