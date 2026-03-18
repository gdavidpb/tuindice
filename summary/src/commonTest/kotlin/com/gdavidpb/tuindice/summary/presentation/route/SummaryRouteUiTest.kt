package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
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
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				isCameraAvailable = true,
				onNavigateToUpdatePassword = { outdatedPasswordNavigations++ },
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
		assertTrue(shownSnackBars.isEmpty())
		assertNodeHidden(SummaryUiTags.ProfilePicturePickAction)
		assertNodeHidden(SummaryUiTags.RemoveProfilePictureMessage)
	}

	@Test
	fun when_profilePictureSettingsActionTriggered_then_showsDialogWithRemoveOption() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				isCameraAvailable = true,
				onNavigateToUpdatePassword = {},
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

		waitUntilNodeExists(SummaryUiTags.ProfilePicturePickAction)
		assertNodeVisible(SummaryUiTags.ProfilePicturePickAction)
		assertNodeVisible(SummaryUiTags.ProfilePictureTakeAction)
		assertNodeVisible(SummaryUiTags.ProfilePictureRemoveAction)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_profilePictureEditTappedFromUi_then_showsDialogWithRemoveOption() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				isCameraAvailable = true,
				onNavigateToUpdatePassword = {},
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

		waitUntilNodeExists(SummaryUiTags.ProfilePicturePickAction)
		assertNodeVisible(SummaryUiTags.ProfilePicturePickAction)
		assertNodeVisible(SummaryUiTags.ProfilePictureTakeAction)
		assertNodeVisible(SummaryUiTags.ProfilePictureRemoveAction)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_removeProfilePictureActionTriggered_then_showsRemoveConfirmationDialog() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()

		setTuIndiceTestContent {
			SummaryRoute(
				isCameraAvailable = true,
				onNavigateToUpdatePassword = {},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository
			)
		}

		runOnIdle {
			viewModel.removeProfilePictureAction()
		}

		waitUntilNodeExists(SummaryUiTags.RemoveProfilePictureMessage)
		assertNodeVisible(SummaryUiTags.RemoveProfilePictureMessage)
	}

	@Test
	fun when_removeTappedFromSettingsDialog_then_showsRemoveConfirmationDialog() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()

		setTuIndiceTestContent {
			SummaryRoute(
				isCameraAvailable = true,
				onNavigateToUpdatePassword = {},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository
			)
		}

		runOnIdle {
			viewModel.openProfilePictureSettingsAction()
		}

		waitUntilNodeExists(SummaryUiTags.ProfilePictureRemoveAction)
		onNodeWithText("Remover foto").performClick()

		waitUntilNodeExists(SummaryUiTags.RemoveProfilePictureMessage)
		assertNodeVisible(SummaryUiTags.RemoveProfilePictureMessage)
		assertNodeHidden(SummaryUiTags.ProfilePicturePickAction)
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
				isCameraAvailable = true,
				onNavigateToUpdatePassword = {
					outdatedPasswordNavigations++
				},
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
				isCameraAvailable = true,
				onNavigateToUpdatePassword = {
					outdatedPasswordNavigations++
				},
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
	fun when_profilePictureSettingsActionTriggeredWithoutProfilePicture_then_showsDialogWithoutRemoveOption() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel(
			userRepository = RecordingUserRepository(
				users = flow {
					emit(DEFAULT_SUMMARY_USER.copy(pictureUrl = ""))
				}
			)
		)
		val syncStatusRepository = FakeSyncStatusRepository()

		setTuIndiceTestContent {
			SummaryRoute(
				isCameraAvailable = true,
				onNavigateToUpdatePassword = {},
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

		waitUntilNodeExists(SummaryUiTags.ProfilePicturePickAction)
		assertNodeVisible(SummaryUiTags.ProfilePicturePickAction)
		assertNodeVisible(SummaryUiTags.ProfilePictureTakeAction)
		assertNodeHidden(SummaryUiTags.ProfilePictureRemoveAction)
	}

	@Test
	fun when_profilePictureEditTappedFromUiWithoutProfilePicture_then_showsDialogWithoutRemoveOption() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel(
			userRepository = RecordingUserRepository(
				users = flow {
					emit(DEFAULT_SUMMARY_USER.copy(pictureUrl = ""))
				}
			)
		)
		val syncStatusRepository = FakeSyncStatusRepository()

		setTuIndiceTestContent {
			SummaryRoute(
				isCameraAvailable = true,
				onNavigateToUpdatePassword = {},
				showSnackBar = {},
				viewModel = viewModel,
				syncStatusRepository = syncStatusRepository
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		waitUntilNodeExists(SummaryUiTags.ProfilePicturePickAction)
		assertNodeVisible(SummaryUiTags.ProfilePicturePickAction)
		assertNodeVisible(SummaryUiTags.ProfilePictureTakeAction)
		assertNodeHidden(SummaryUiTags.ProfilePictureRemoveAction)
	}

	@Test
	fun when_confirmRemoveProfilePictureActionTriggered_then_showsSnackBarWithoutOutdatedNavigation() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		val syncStatusRepository = FakeSyncStatusRepository()
		var navigateOutdatedCalls = 0
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				isCameraAvailable = true,
				onNavigateToUpdatePassword = { navigateOutdatedCalls++ },
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
				isCameraAvailable = true,
				onNavigateToUpdatePassword = {},
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

	private fun ComposeUiTest.waitUntilNodeExists(tag: String) {
		waitUntil(timeoutMillis = 2_000) {
			runCatching { onNodeWithTag(tag).fetchSemanticsNode() }.isSuccess
		}
	}
}
