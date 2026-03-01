package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.PlatformUri
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_PROFILE_PICTURE
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.summary.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SummaryUseCaseContractTest {
	@Test
	fun getUserUseCase_emitsLoadingThenData_fromRepositoryFlow() = runTest {
		val useCase = GetUserUseCase(
			userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER)),
			exceptionHandler = GetUserExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)

		useCase.execute(Unit).test {
			assertEquals(DEFAULT_SUMMARY_USER, awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun uploadProfilePictureUseCase_emitsLoadingThenData_andDelegatesUri() = runTest {
		val repository = RecordingUserRepository(profilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE)
		val useCase = UploadProfilePictureUseCase(
			userRepository = repository,
			paramsValidator = UploadProfilePictureParamsValidator(),
			exceptionHandler = UploadProfilePictureExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)
		val uri = PlatformUri("content://profile/new.jpg")

		useCase.execute(uri).test {
			assertEquals(DEFAULT_SUMMARY_PROFILE_PICTURE.url, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(listOf(uri), repository.uploadCalls)
	}

	@Test
	fun uploadProfilePictureUseCase_rejectsEmptyUri_withInvalidSourceError() = runTest {
		val useCase = UploadProfilePictureUseCase(
			userRepository = RecordingUserRepository(),
			paramsValidator = UploadProfilePictureParamsValidator(),
			exceptionHandler = UploadProfilePictureExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)

		useCase.execute(PlatformUri("")).test {
			val error = awaitLoadingThenError(this)
			assertEquals(ProfilePictureUseCaseError.InvalidSource, error.error)
			awaitComplete()
		}
	}
}
