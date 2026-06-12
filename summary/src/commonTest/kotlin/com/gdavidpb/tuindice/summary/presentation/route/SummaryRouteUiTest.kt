package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_PROFILE_PICTURE
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.summary.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SummaryRouteUiTest {
	@Test
	fun when_initialLoadSucceeds_then_routeRendersContentWithoutDialogsOrSnackBar() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()
		var outdatedPasswordNavigations = 0
		val profilePictureSettingsNavigations = mutableListOf<Boolean>()
		var removeProfilePictureConfirmationNavigations = 0
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = { outdatedPasswordNavigations++ },
				onNavigateToProfilePictureSettingsDialog = { showRemove ->
					profilePictureSettingsNavigations += showRemove
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {
					removeProfilePictureConfirmationNavigations++
				},
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.isUserRefreshing == false
		}

		assertEquals(0, outdatedPasswordNavigations)
		assertTrue(profilePictureSettingsNavigations.isEmpty())
		assertEquals(0, removeProfilePictureConfirmationNavigations)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_profilePictureSettingsActionTriggered_then_requestsDialogWithRemoveOption() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()
		val profilePictureSettingsNavigations = mutableListOf<Boolean>()
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = { showRemove ->
					profilePictureSettingsNavigations += showRemove
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.isUserRefreshing == false
		}

		runOnIdle {
			viewModel.openProfilePictureSettingsAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			profilePictureSettingsNavigations.isNotEmpty()
		}

		assertEquals(listOf(true), profilePictureSettingsNavigations)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_profilePictureEditTappedFromUi_then_requestsDialogWithRemoveOption() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()
		val profilePictureSettingsNavigations = mutableListOf<Boolean>()
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = { showRemove ->
					profilePictureSettingsNavigations += showRemove
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.isUserRefreshing == false
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			profilePictureSettingsNavigations.isNotEmpty()
		}

		assertEquals(listOf(true), profilePictureSettingsNavigations)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_userRefreshIsRunning_then_profilePictureEditRemainsDisabledUntilRefreshCompletes() = runTuIndiceUiTest {
		val userRepository = BlockingRefreshUserRepository()
		val viewModel = createSummaryViewModel(userRepository = userRepository)
		val syncStatusRepository = FakeSyncStatusRepository()
		val profilePictureSettingsNavigations = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceAnimationsEnabled provides false) {
				SummaryRoute(
					onNavigateToUpdatePassword = {},
					onNavigateToProfilePictureSettingsDialog = { showRemove ->
						profilePictureSettingsNavigations += showRemove
					},
					onNavigateToRemoveProfilePictureConfirmationDialog = {},
					showSnackBar = {},
					viewModel = viewModel,
					syncStatusRepository = syncStatusRepository,
					syncRepository = FakeSyncRepository()
				)
			}
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.isUserRefreshing == true &&
				userRepository.updateUserCalls == 1
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).assertIsNotEnabled()
		assertTrue(profilePictureSettingsNavigations.isEmpty())

		userRepository.completeRefresh()

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.isUserRefreshing == false
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).assertIsEnabled()
		assertTrue(profilePictureSettingsNavigations.isEmpty())
	}

	@Test
	fun when_removeProfilePictureActionTriggered_then_requestsRemoveConfirmationDialog() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()
		var removeProfilePictureConfirmationNavigations = 0

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {
					removeProfilePictureConfirmationNavigations++
				},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		runOnIdle {
			viewModel.removeProfilePictureAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			removeProfilePictureConfirmationNavigations > 0
		}

		assertEquals(1, removeProfilePictureConfirmationNavigations)
	}

	@Test
	fun when_initialLoadFailsWithGenericError_then_routeForwardsSnackBarEffect() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel(
			userRepository = RecordingUserRepository(
				users = flow { throw IllegalStateException("boom") }
			)
		)
		val syncStatusRepository = FakeSyncStatusRepository()
		val shownSnackBars = mutableListOf<SnackBarMessage>()
		var outdatedPasswordNavigations = 0

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {
					outdatedPasswordNavigations++
				},
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			shownSnackBars.isNotEmpty()
		}

		assertEquals("¡Ha ocurrido un error!", shownSnackBars.first().message)
		assertEquals(0, outdatedPasswordNavigations)
	}

	@Test
	fun when_loadFailsWithConflictAfterContent_then_routeShowsGenericError() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel(
			userRepository = RecordingUserRepository(
				users = flow {
					emit(DEFAULT_SUMMARY_USER)
				},
				updateThrowable = clientRequestException(
					statusCode = HttpStatusCode.Conflict,
					path = "/users/v1"
				)
			)
		)
		val syncStatusRepository = FakeSyncStatusRepository()
		var outdatedPasswordNavigations = 0
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {
					outdatedPasswordNavigations++
				},
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			shownSnackBars.isNotEmpty()
		}

		assertEquals(0, outdatedPasswordNavigations)
		assertEquals("¡Ha ocurrido un error!", shownSnackBars.first().message)
	}

	@Test
	fun when_profilePictureSettingsActionTriggeredWithoutProfilePicture_then_requestsDialogWithoutRemoveOption() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel(
			userRepository = RecordingUserRepository(
				users = flow {
					emit(DEFAULT_SUMMARY_USER.copy(pictureUrl = ""))
				}
			)
		)
		val syncStatusRepository = FakeSyncStatusRepository()
		val profilePictureSettingsNavigations = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = { showRemove ->
					profilePictureSettingsNavigations += showRemove
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.isUserRefreshing == false
		}

		runOnIdle {
			viewModel.openProfilePictureSettingsAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			profilePictureSettingsNavigations.isNotEmpty()
		}

		assertEquals(listOf(false), profilePictureSettingsNavigations)
	}

	@Test
	fun when_profilePictureEditTappedFromUiWithoutProfilePicture_then_requestsDialogWithoutRemoveOption() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel(
			userRepository = RecordingUserRepository(
				users = flow {
					emit(DEFAULT_SUMMARY_USER.copy(pictureUrl = ""))
				}
			)
		)
		val syncStatusRepository = FakeSyncStatusRepository()
		val profilePictureSettingsNavigations = mutableListOf<Boolean>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = { showRemove ->
					profilePictureSettingsNavigations += showRemove
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			profilePictureSettingsNavigations.isNotEmpty()
		}

		assertEquals(listOf(false), profilePictureSettingsNavigations)
	}

	@Test
	fun when_confirmRemoveProfilePictureActionTriggered_then_showsSnackBarWithoutOutdatedNavigation() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()
		var navigateOutdatedCalls = 0
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = { navigateOutdatedCalls++ },
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
		}

		runOnIdle {
			viewModel.confirmRemoveProfilePictureAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			shownSnackBars.isNotEmpty()
		}

		assertEquals(0, navigateOutdatedCalls)
		assertTrue(shownSnackBars.first().message.isNotBlank())
	}

	@Test
	fun when_confirmRemoveProfilePictureActionSucceeds_then_stateClearsPictureWithoutWaitingForObservation() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
		}

		runOnIdle {
			viewModel.confirmRemoveProfilePictureAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			(viewModel.state.value as? Summary.State.Content)?.profilePictureUrl == ""
		}

		assertEquals("", assertIs<Summary.State.Content>(viewModel.state.value).profilePictureUrl)
	}

	@Test
	fun when_retryTappedAfterFailure_then_routeRequestsRefreshAgain() = runTuIndiceUiTest {
		val userRepository = CountingFailingRefreshUserRepository()
		val viewModel = createSummaryViewModel(userRepository = userRepository)
		val syncStatusRepository = FakeSyncStatusRepository()
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository,
				syncRepository = FakeSyncRepository()
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			userRepository.updateUserCalls > 0 && shownSnackBars.isNotEmpty()
		}

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			userRepository.updateUserCalls >= 2
		}

		assertTrue(userRepository.updateUserCalls >= 2)
	}

	private fun createSummaryViewModel(
		userRepository: UserRepository = RecordingUserRepository(),
		dispatchers: TuIndiceDispatchers = TestTuIndiceDispatchers(Dispatchers.Unconfined)
	): SummaryViewModel {
		return SummaryViewModel(
			screenMachine = SummaryMachine(
				observeUserUseCase = ObserveUserUseCase(
					userRepository = userRepository,
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

	private class CountingFailingRefreshUserRepository : UserRepository {
		var updateUserCalls = 0
			private set

		override suspend fun observeUserFlow() = emptyFlow<User>()

		override suspend fun updateUser() {
			updateUserCalls++
			throw IllegalStateException("summary-route-retry")
		}

		override suspend fun uploadProfilePicture(file: PlatformFile) = DEFAULT_SUMMARY_PROFILE_PICTURE

		override suspend fun removeProfilePicture() = Unit
	}

	private class BlockingRefreshUserRepository : UserRepository {
		private val releaseRefresh = CompletableDeferred<Unit>()
		var updateUserCalls = 0
			private set

		override suspend fun observeUserFlow() = flowOf(DEFAULT_SUMMARY_USER)

		override suspend fun updateUser() {
			updateUserCalls++
			releaseRefresh.await()
		}

		override suspend fun uploadProfilePicture(file: PlatformFile) = DEFAULT_SUMMARY_PROFILE_PICTURE

		override suspend fun removeProfilePicture() = Unit

		fun completeRefresh() {
			releaseRefresh.complete(Unit)
		}
	}
}
