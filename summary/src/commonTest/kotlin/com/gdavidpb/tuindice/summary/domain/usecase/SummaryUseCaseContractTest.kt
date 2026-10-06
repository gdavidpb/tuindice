package com.gdavidpb.tuindice.summary.domain.usecase

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.summary.domain.model.ObservedSync
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_PROFILE_PICTURE
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeSyncProgressRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
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
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(DEFAULT_SUMMARY_USER, awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun getCameraAvailabilityUseCase_emitsLoadingThenData_withWhatTheDeviceSays() = runTest {
		for (hasCamera in listOf(true, false)) {
			val useCase = GetCameraAvailabilityUseCase(
				deviceInfoRepository = FakeDeviceInfoRepository(deviceHasCamera = hasCamera),
				reportingRepository = RecordingReportingRepository()
			)

			useCase.execute(Unit).test {
				assertEquals(hasCamera, awaitLoadingThenData(this))
				awaitComplete()
			}
		}
	}

	@Test
	fun getCameraAvailabilityUseCase_reportsAndEmitsError_whenTheDeviceCannotSay() = runTest {
		val failure = IllegalStateException("summary-camera")
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetCameraAvailabilityUseCase(
			deviceInfoRepository = object : DeviceInfoRepository by FakeDeviceInfoRepository() {
				override fun hasCamera(): Boolean = throw failure
			},
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			assertIs<UseCaseState.Error<*>>(awaitItem())
			awaitComplete()
		}

		assertEquals(failure, reportingRepository.loggedExceptions.single())
	}

	@Test
	fun observeSyncUseCase_emitsLoadingThenData_fromTheFourSyncFlows() = runTest {
		val useCase = ObserveSyncUseCase(
			syncStatusRepository = FakeSyncStatusRepository(
				initialValue = SyncStatus.RecordAccessDenied,
				initialReport = SyncReport.failedRecordUnavailable(),
				initialLastSuccessfulSyncAt = 1_709_251_200_000L
			),
			syncRepository = FakeSyncProgressRepository(initialSyncInProgress = true),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(
				ObservedSync(
					status = SyncStatus.RecordAccessDenied,
					report = SyncReport.failedRecordUnavailable(),
					lastSuccessfulSyncAt = 1_709_251_200_000L,
					isInProgress = true
				),
				awaitLoadingThenData(this)
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun observeSyncUseCase_emitsAgain_whenAnyOfTheFourSyncFlowsChanges() = runTest {
		val syncStatusRepository = FakeSyncStatusRepository()
		val syncRepository = FakeSyncProgressRepository()
		val useCase = ObserveSyncUseCase(
			syncStatusRepository = syncStatusRepository,
			syncRepository = syncRepository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			val initial = awaitLoadingThenData(this)

			assertEquals(
				ObservedSync(
					status = SyncStatus.Healthy,
					report = SyncReport.success(),
					lastSuccessfulSyncAt = null,
					isInProgress = false
				),
				initial
			)

			syncRepository.syncInProgress.value = true
			val syncing = awaitSync()
			assertEquals(initial.copy(isInProgress = true), syncing)

			syncStatusRepository.setSyncStatus(SyncStatus.Unavailable)
			val unavailable = awaitSync()
			assertEquals(syncing.copy(status = SyncStatus.Unavailable), unavailable)

			syncStatusRepository.setSyncReport(SyncReport.partialEnrollmentUnavailable())
			val partial = awaitSync()
			assertEquals(
				unavailable.copy(report = SyncReport.partialEnrollmentUnavailable()),
				partial
			)

			syncStatusRepository.setLastSuccessfulSyncAt(1_709_251_200_000L)
			val synced = awaitSync()
			assertEquals(partial.copy(lastSuccessfulSyncAt = 1_709_251_200_000L), synced)

			syncRepository.syncInProgress.value = false
			assertEquals(synced.copy(isInProgress = false), awaitSync())

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

	private suspend fun ReceiveTurbine<UseCaseState<ObservedSync, Nothing>>.awaitSync(): ObservedSync {
		return assertIs<UseCaseState.Data<ObservedSync>>(awaitItem()).value
	}
}
