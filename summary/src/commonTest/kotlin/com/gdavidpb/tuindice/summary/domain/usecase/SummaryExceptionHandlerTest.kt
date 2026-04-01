package com.gdavidpb.tuindice.summary.domain.usecase

import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.UpdateUserUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryExceptionHandlerTest {
	@Test
	fun updateUserExceptionHandler_mapsNotFound() {
		val throwable = clientRequestException(HttpStatusCode.NotFound, path = "/users/v1")

		val actual = UpdateUserExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(UpdateUserUseCaseError.NotFound, actual)
	}

	@Test
	fun updateUserExceptionHandler_leavesConflictUnhandled() {
		val throwable = clientRequestException(HttpStatusCode.Conflict, path = "/users/v1")

		val actual = UpdateUserExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(null, actual)
	}

	@Test
	fun uploadProfilePictureExceptionHandler_mapsIllegalArgumentToUnableToEncode() {
		val throwable = IllegalArgumentException()

		val actual = UploadProfilePictureExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(ProfilePictureUseCaseError.UnableToEncode, actual)
	}

	@Test
	fun uploadProfilePictureExceptionHandler_mapsUnsupportedMediaType() {
		val throwable = clientRequestException(HttpStatusCode.UnsupportedMediaType, path = "/users/v1/picture")

		val actual = UploadProfilePictureExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(ProfilePictureUseCaseError.UnableToEncode, actual)
	}

	@Test
	fun uploadProfilePictureExceptionHandler_mapsPayloadTooLarge() {
		val throwable = clientRequestException(HttpStatusCode.PayloadTooLarge, path = "/users/v1/picture")

		val actual = UploadProfilePictureExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(ProfilePictureUseCaseError.SizeExceeded, actual)
	}

	@Test
	fun removeProfilePictureExceptionHandler_mapsConnectionToNoConnection() {
		val throwable = IllegalStateException("network is unreachable")

		val actual = RemoveProfilePictureExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = false)
		).parseException(throwable)

		val error = assertIs<ProfilePictureUseCaseError.NoConnection>(actual)
		assertEquals(false, error.isNetworkAvailable)
	}

	@Test
	fun removeProfilePictureExceptionHandler_mapsNotFound() {
		val throwable = clientRequestException(HttpStatusCode.NotFound, path = "/users/v1/picture")

		val actual = RemoveProfilePictureExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(ProfilePictureUseCaseError.NotFound, actual)
	}
}
