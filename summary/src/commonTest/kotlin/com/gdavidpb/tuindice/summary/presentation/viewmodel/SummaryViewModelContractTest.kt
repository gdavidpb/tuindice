package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.GetCameraAvailabilityUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSyncUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.model.SummarySyncItem
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_PROFILE_PICTURE
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeSyncProgressRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun initialAction_observesSummary_andUserActionEmitsPickerEffect() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(dispatchers = dispatchers)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				val content = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first()
				}
				assertEquals("Ana Diaz", content.name)
				assertEquals(DEFAULT_SUMMARY_USER.pictureUrl, content.profilePictureUrl)

				val pickerEffect = async { viewModel.effect.first() }
				viewModel.pickProfilePictureAction()

				assertIs<Summary.Effect.OpenPicker>(pickerEffect.await())
			} finally {
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun openProfilePictureSettings_tellsTheDialogWhatTheDeviceAndTheStateAllow() = runTest {
		withMainDispatcher { dispatchers ->
			// Removing is offered by the state (there is a picture or not), taking a picture by
			// the device (it has a camera or not): every pair travels in the one effect.
			for (hasPicture in listOf(true, false)) {
				for (hasCamera in listOf(true, false)) {
					val viewModel = createViewModel(
						dispatchers = dispatchers,
						userRepository = RecordingUserRepository(
							users = flowOf(
								DEFAULT_SUMMARY_USER.copy(
									pictureUrl = if (hasPicture) DEFAULT_SUMMARY_USER.pictureUrl else ""
								)
							)
						),
						deviceInfoRepository = FakeDeviceInfoRepository(deviceHasCamera = hasCamera)
					)
					val stateCollector = backgroundScope.launchStateCollector(
						flow = viewModel.state,
						testScheduler = testScheduler
					)

					try {
						viewModel.awaitContent { true }

						val effect = async { viewModel.effect.first() }
						viewModel.openProfilePictureSettingsAction()

						val dialog = assertIs<Summary.Effect.ShowProfilePictureSettingsDialog>(
							effect.await()
						)
						assertEquals(hasPicture, dialog.showRemove)
						assertEquals(hasCamera, dialog.isCameraAvailable)
					} finally {
						stateCollector.cancel()
					}
				}
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun openProfilePictureSettings_stillOpensWithoutTheTakeOption_whenTheDeviceCannotSay() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(
				dispatchers = dispatchers,
				deviceInfoRepository = object : DeviceInfoRepository by FakeDeviceInfoRepository() {
					override fun hasCamera(): Boolean = error("summary-view-model-camera")
				}
			)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.awaitContent { true }

				val effect = async { viewModel.effect.first() }
				viewModel.openProfilePictureSettingsAction()

				val dialog = assertIs<Summary.Effect.ShowProfilePictureSettingsDialog>(
					effect.await()
				)
				assertEquals(true, dialog.showRemove)
				assertEquals(false, dialog.isCameraAvailable)
			} finally {
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun openProfilePictureSettings_doesNotAskTheDevice_whileThePictureIsBeingChanged() = runTest {
		withMainDispatcher { dispatchers ->
			val deviceInfoRepository = CountingDeviceInfoRepository()
			val userRepository = BlockingUploadUserRepository()
			val viewModel = createViewModel(
				dispatchers = dispatchers,
				userRepository = userRepository,
				deviceInfoRepository = deviceInfoRepository
			)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.awaitContent { true }

				viewModel.uploadProfilePictureAction(PlatformFile("/tmp/profile.jpg"))
				viewModel.awaitContent { content -> content.isProfilePictureLoading }

				viewModel.openProfilePictureSettingsAction()
				// The picker goes through the same queue: once its effect is out, the open
				// request before it has been resolved, and it opened nothing.
				val effect = async { viewModel.effect.first() }
				viewModel.pickProfilePictureAction()

				assertIs<Summary.Effect.OpenPicker>(effect.await())
				assertEquals(0, deviceInfoRepository.hasCameraCalls)
			} finally {
				userRepository.finishUpload()
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun refreshEnqueuedBeforeObserve_stillReachesContent() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(dispatchers = dispatchers)

			// Mirrors the real startup order on Android: the route's LaunchedEffect
			// enqueues the refresh during composition, before collectAsStateWithLifecycle
			// starts the machine loop and queues the initial ObserveSummary — so Refresh
			// is processed first and the machine is already in Loading when Observe
			// arrives. Regression test for the Loading deadlock.
			viewModel.refreshSummaryAction()

			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				val content = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Content>()
						.first()
				}
				assertEquals("Ana Diaz", content.name)
			} finally {
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun syncObservation_isFollowedByContent_onEveryChange() = runTest {
		withMainDispatcher { dispatchers ->
			val syncStatusRepository = FakeSyncStatusRepository()
			val syncRepository = FakeSyncProgressRepository()
			val viewModel = createViewModel(
				dispatchers = dispatchers,
				syncStatusRepository = syncStatusRepository,
				syncRepository = syncRepository
			)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				val initial = viewModel.awaitContent { true }.sync
				assertEquals(SummarySyncItem(), initial)

				syncRepository.syncInProgress.value = true
				val syncing = viewModel.awaitContent { content -> content.sync.isSyncing }.sync
				assertEquals(initial.copy(isSyncing = true), syncing)

				syncStatusRepository.setSyncStatus(SyncStatus.Unavailable)
				val unavailable = viewModel
					.awaitContent { content -> content.sync.status == SyncStatus.Unavailable }
					.sync
				assertEquals(syncing.copy(status = SyncStatus.Unavailable), unavailable)

				syncStatusRepository.setSyncReport(SyncReport.partialEnrollmentUnavailable())
				val partial = viewModel
					.awaitContent { content -> content.sync.report.hasUnavailableSource }
					.sync
				assertEquals(
					unavailable.copy(report = SyncReport.partialEnrollmentUnavailable()),
					partial
				)

				syncStatusRepository.setLastSuccessfulSyncAt(LAST_SUCCESSFUL_SYNC_AT)
				val synced = viewModel
					.awaitContent { content -> content.sync.lastSuccessfulSyncAt != null }
					.sync
				assertEquals(partial.copy(lastSuccessfulSyncAt = LAST_SUCCESSFUL_SYNC_AT), synced)

				syncRepository.syncInProgress.value = false
				val idle = viewModel.awaitContent { content -> !content.sync.isSyncing }.sync
				assertEquals(synced.copy(isSyncing = false), idle)
			} finally {
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun syncObservation_isKeptAcrossUserObservations() = runTest {
		withMainDispatcher { dispatchers ->
			val users = MutableSharedFlow<User>()
			val viewModel = createViewModel(
				dispatchers = dispatchers,
				userRepository = RecordingUserRepository(users = users),
				syncStatusRepository = FakeSyncStatusRepository(
					initialValue = SyncStatus.RecordAccessDenied
				)
			)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				// No user yet: Idle is the one told what the sync says.
				withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Idle>()
						.first { state -> state.sync.status == SyncStatus.RecordAccessDenied }
					users.subscriptionCount.first { count -> count > 0 }
				}

				// The user's observation builds the content anew, the first time and every time
				// after: what the sync said must not go back to the defaults with it.
				users.emit(DEFAULT_SUMMARY_USER)
				val first = viewModel.awaitContent { true }
				assertEquals(SyncStatus.RecordAccessDenied, first.sync.status)

				users.emit(DEFAULT_SUMMARY_USER.copy(careerName = "Ingenieria Electrica"))
				val second = viewModel.awaitContent { content ->
					content.careerName == "Ingenieria Electrica"
				}
				assertEquals(SyncStatus.RecordAccessDenied, second.sync.status)
			} finally {
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun syncObservation_reachesFailed_forAnAccountWithNoUserStored() = runTest {
		withMainDispatcher { dispatchers ->
			val userRepository = BlockingFailingRefreshUserRepository()
			val syncStatusRepository = FakeSyncStatusRepository()
			val syncRepository = FakeSyncProgressRepository(initialSyncInProgress = true)
			val viewModel = createViewModel(
				dispatchers = dispatchers,
				userRepository = userRepository,
				syncStatusRepository = syncStatusRepository,
				syncRepository = syncRepository
			)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.refreshSummaryAction()

				// Whichever comes first, the refresh or the first sync observation, Loading ends up
				// knowing what Idle was told.
				val loading = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Loading>()
						.first { state -> state.sync.isSyncing }
				}
				assertEquals(SummarySyncItem(isSyncing = true), loading.sync)

				// The sync learns why there is no user while the refresh is still running: nothing
				// is observed again after it, so Failed has to inherit it from Loading.
				syncStatusRepository.setSyncStatus(SyncStatus.NewStudentNoRecord)
				syncStatusRepository.setSyncReport(SyncReport.failedRecordUnavailable())
				withTimeout(3_000) {
					viewModel.state.first { state ->
						state is Summary.State.Loading &&
							state.sync.report == SyncReport.failedRecordUnavailable()
					}
				}

				userRepository.failRefresh()

				val failed = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Failed>()
						.first()
				}
				assertEquals(
					SummarySyncItem(
						status = SyncStatus.NewStudentNoRecord,
						report = SyncReport.failedRecordUnavailable(),
						isSyncing = true
					),
					failed.sync
				)

				// And Failed keeps listening: the retry is enabled again when the sync stops.
				syncRepository.syncInProgress.value = false
				val settled = withTimeout(3_000) {
					viewModel.state
						.filterIsInstance<Summary.State.Failed>()
						.first { state -> !state.sync.isSyncing }
				}
				assertEquals(failed.sync.copy(isSyncing = false), settled.sync)
			} finally {
				stateCollector.cancel()
			}
		}
	}

	private suspend fun SummaryViewModel.awaitContent(
		predicate: (Summary.State.Content) -> Boolean
	): Summary.State.Content {
		return withTimeout(3_000) {
			state
				.filterIsInstance<Summary.State.Content>()
				.first { content -> predicate(content) }
		}
	}

	private fun createViewModel(
		dispatchers: TuIndiceDispatchers,
		userRepository: UserRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER)),
		syncStatusRepository: SyncStatusRepository = FakeSyncStatusRepository(),
		syncRepository: SyncRepository = FakeSyncRepository(),
		deviceInfoRepository: DeviceInfoRepository = FakeDeviceInfoRepository()
	): SummaryViewModel {
		return SummaryViewModel(
			screenMachine = SummaryMachine(
				observeUserUseCase = ObserveUserUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository()
				),
				observeSyncUseCase = ObserveSyncUseCase(
					syncStatusRepository = syncStatusRepository,
					syncRepository = syncRepository,
					reportingRepository = RecordingReportingRepository()
				),
				getCameraAvailabilityUseCase = GetCameraAvailabilityUseCase(
					deviceInfoRepository = deviceInfoRepository,
					reportingRepository = RecordingReportingRepository()
				),
				updateUserUseCase = UpdateUserUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UpdateUserExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				uploadProfilePictureUseCase = UploadProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = UploadProfilePictureExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				removeProfilePictureUseCase = RemoveProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = RemoveProfilePictureExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				)
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}

	// An account with nothing stored: the user is never observed and the refresh fails when told.
	private class BlockingFailingRefreshUserRepository : UserRepository {
		private val refreshFailure = CompletableDeferred<Unit>()

		override suspend fun observeUserFlow(): Flow<User> = emptyFlow()

		override suspend fun updateUser() {
			refreshFailure.await()
			error("summary-view-model-refresh")
		}

		override suspend fun uploadProfilePicture(file: PlatformFile) = DEFAULT_SUMMARY_PROFILE_PICTURE

		override suspend fun removeProfilePicture() = Unit

		fun failRefresh() {
			refreshFailure.complete(Unit)
		}
	}

	// An upload that stays in flight until told: the picture is loading for as long as it does.
	private class BlockingUploadUserRepository : UserRepository {
		private val uploadFinished = CompletableDeferred<Unit>()

		override suspend fun observeUserFlow(): Flow<User> = flowOf(DEFAULT_SUMMARY_USER)

		override suspend fun updateUser() = Unit

		override suspend fun uploadProfilePicture(file: PlatformFile): ProfilePicture {
			uploadFinished.await()
			return DEFAULT_SUMMARY_PROFILE_PICTURE
		}

		override suspend fun removeProfilePicture() = Unit

		fun finishUpload() {
			uploadFinished.complete(Unit)
		}
	}

	private class CountingDeviceInfoRepository : DeviceInfoRepository by FakeDeviceInfoRepository() {
		var hasCameraCalls = 0
			private set

		override fun hasCamera(): Boolean {
			hasCameraCalls++
			return true
		}
	}

	private companion object {
		const val LAST_SUCCESSFUL_SYNC_AT = 1_709_251_200_000L
	}
}
