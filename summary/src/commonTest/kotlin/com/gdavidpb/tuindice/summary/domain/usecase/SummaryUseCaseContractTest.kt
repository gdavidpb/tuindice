package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
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
import io.github.vinceglb.filekit.PlatformFile
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
	fun uploadProfilePictureUseCase_emitsLoadingThenData_andDelegatesFile() = runTest {
		val repository = RecordingUserRepository(profilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE)
		val useCase = UploadProfilePictureUseCase(
			userRepository = repository,
			paramsValidator = UploadProfilePictureParamsValidator(),
			exceptionHandler = UploadProfilePictureExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)
		val file = PlatformFile("content://profile/new.jpg")

		useCase.execute(file).test {
			assertEquals(DEFAULT_SUMMARY_PROFILE_PICTURE.url, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(listOf(file), repository.uploadCalls)
	}

	@Test
	fun uploadProfilePictureUseCase_rejectsInvalidFileSource_withInvalidSourceError() = runTest {
		val useCase = UploadProfilePictureUseCase(
			userRepository = RecordingUserRepository(),
			paramsValidator = UploadProfilePictureParamsValidator(),
			exceptionHandler = UploadProfilePictureExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true),
				reportingRepository = RecordingReportingRepository()
			)
		)

		useCase.execute(PlatformFile("/tmp")).test {
			val error = awaitLoadingThenError(this)
			assertEquals(ProfilePictureUseCaseError.InvalidSource, error.error)
			awaitComplete()
		}
	}
}
