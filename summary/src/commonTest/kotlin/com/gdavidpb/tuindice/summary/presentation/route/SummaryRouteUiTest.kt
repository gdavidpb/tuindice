package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.GetUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.GetUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.validator.UploadProfilePictureParamsValidator
import com.gdavidpb.tuindice.summary.presentation.action.ConfirmRemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.LoadSummaryActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.OpenProfilePictureSettingsActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.PickProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.RemoveProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.TakeProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.action.UploadProfilePictureActionProcessor
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_PROFILE_PICTURE
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.summary.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.flow
import kotlin.test.Test
import kotlin.test.assertEquals
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
				syncStatusRepository = syncStatusRepository
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
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
				syncStatusRepository = syncStatusRepository
			)
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
				syncStatusRepository = syncStatusRepository
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			profilePictureSettingsNavigations.isNotEmpty()
		}

		assertEquals(listOf(true), profilePictureSettingsNavigations)
		assertTrue(shownSnackBars.isEmpty())
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
				syncStatusRepository = syncStatusRepository
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
				syncStatusRepository = syncStatusRepository
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
					throw clientRequestException(
						statusCode = HttpStatusCode.Conflict,
						path = "/users/v1"
					)
				}
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
				syncStatusRepository = syncStatusRepository
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
				syncStatusRepository = syncStatusRepository
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
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
				syncStatusRepository = syncStatusRepository
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
				syncStatusRepository = syncStatusRepository
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
	fun when_retryTappedAfterFailure_then_routeRequestsLoadAgain() = runTuIndiceUiTest {
		val userRepository = CountingFailingUserRepository()
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
				syncStatusRepository = syncStatusRepository
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			userRepository.getUserFlowCalls > 0 && shownSnackBars.isNotEmpty()
		}

		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			userRepository.getUserFlowCalls >= 2
		}

		assertTrue(userRepository.getUserFlowCalls >= 2)
	}

	private fun createSummaryViewModel(
		userRepository: UserRepository = RecordingUserRepository()
	): SummaryViewModel {
		return SummaryViewModel(
			loadSummaryActionProcessor = LoadSummaryActionProcessor(
				getUserUseCase = GetUserUseCase(
					userRepository = userRepository,
					exceptionHandler = GetUserExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true),
						reportingRepository = RecordingReportingRepository()
					)
				)
			),
			takeProfilePictureActionProcessor = TakeProfilePictureActionProcessor(),
			pickProfilePictureActionProcessor = PickProfilePictureActionProcessor(),
			uploadProfilePictureActionProcessor = UploadProfilePictureActionProcessor(
				uploadProfilePictureUseCase = UploadProfilePictureUseCase(
					userRepository = userRepository,
					paramsValidator = UploadProfilePictureParamsValidator(),
					exceptionHandler = UploadProfilePictureExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true),
						reportingRepository = RecordingReportingRepository()
					)
				)
			),
			confirmRemoveProfilePictureActionProcessor = ConfirmRemoveProfilePictureActionProcessor(
				removeProfilePictureUseCase = RemoveProfilePictureUseCase(
					userRepository = userRepository,
					exceptionHandler = RemoveProfilePictureExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true),
						reportingRepository = RecordingReportingRepository()
					)
				)
			),
			removeProfilePictureActionProcessor = RemoveProfilePictureActionProcessor(),
			openProfilePictureSettingsActionProcessor = OpenProfilePictureSettingsActionProcessor()
		)
	}

	private class CountingFailingUserRepository : UserRepository {
		var getUserFlowCalls = 0
			private set

		override suspend fun getUserFlow() = flow<User> {
			getUserFlowCalls++
			throw IllegalStateException("summary-route-retry")
		}

		override suspend fun uploadProfilePicture(file: PlatformFile) = DEFAULT_SUMMARY_PROFILE_PICTURE

		override suspend fun removeProfilePicture() = Unit
	}
}
