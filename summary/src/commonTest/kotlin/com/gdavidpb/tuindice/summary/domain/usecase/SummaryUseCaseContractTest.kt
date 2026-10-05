package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.ObservedSummary
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_PROFILE_PICTURE
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeCurrentTermRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryUseCaseContractTest {
	@Test
	fun observeUserUseCase_emitsLoadingThenData_fromRepositoryFlow() = runTest {
		val useCase = ObserveUserUseCase(
			userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER)),
			currentTermRepository = FakeCurrentTermRepository(),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(
				ObservedSummary(user = DEFAULT_SUMMARY_USER, hasCurrentTerm = false),
				awaitLoadingThenData(this)
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun observeUserUseCase_foldsTheCurrentTermIntoTheObservation_andFollowsItsChanges() = runTest {
		val currentTermRepository = FakeCurrentTermRepository(initialValue = true)
		val useCase = ObserveUserUseCase(
			userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER)),
			currentTermRepository = currentTermRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(
				ObservedSummary(user = DEFAULT_SUMMARY_USER, hasCurrentTerm = true),
				awaitLoadingThenData(this)
			)

			// A record refresh that drops the current term re-emits with the same user.
			currentTermRepository.hasCurrentTerm.value = false

			val next = assertIs<UseCaseState.Data<ObservedSummary>>(awaitItem())
			assertEquals(
				ObservedSummary(user = DEFAULT_SUMMARY_USER, hasCurrentTerm = false),
				next.value
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun updateUserUseCase_emitsLoadingThenData_andDelegatesRefresh() = runTest {
		val repository = RecordingUserRepository()
		val useCase = UpdateUserUseCase(
			userRepository = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = UpdateUserExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)

		useCase.execute(Unit).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, repository.updateCalls)
	}

	@Test
	fun uploadProfilePictureUseCase_emitsLoadingThenData_andDelegatesFile() = runTest {
		val repository = RecordingUserRepository(profilePicture = DEFAULT_SUMMARY_PROFILE_PICTURE)
		val useCase = UploadProfilePictureUseCase(
			userRepository = repository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = UploadProfilePictureExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)
		val file = PlatformFile("content://profile/new.jpg")

		useCase.execute(file).test {
			assertEquals(DEFAULT_SUMMARY_PROFILE_PICTURE.url, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(listOf(file), repository.uploadCalls)
	}
}
