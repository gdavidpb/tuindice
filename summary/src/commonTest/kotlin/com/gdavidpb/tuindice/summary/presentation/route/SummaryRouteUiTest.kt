package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
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
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_PROFILE_PICTURE
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.summary.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
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
	fun when_initialLoadSucceeds_then_routeRendersContentWithoutNavigationOrSnackBar() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		var outdatedPasswordNavigations = 0
		var profilePictureSettingsNavigations = 0
		var removeConfirmationNavigations = 0
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = { outdatedPasswordNavigations++ },
				onNavigateToProfilePictureSettingsDialog = { profilePictureSettingsNavigations++ },
				onNavigateToRemoveProfilePictureConfirmationDialog = { removeConfirmationNavigations++ },
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
		}

		assertEquals(0, outdatedPasswordNavigations)
		assertEquals(0, profilePictureSettingsNavigations)
		assertEquals(0, removeConfirmationNavigations)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_profilePictureSettingsActionTriggered_then_navigatesWithShowRemove() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		var showRemove: Boolean? = null
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = { shouldShowRemove ->
					showRemove = shouldShowRemove
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.openProfilePictureSettingsAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			showRemove != null
		}

		assertEquals(true, showRemove)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_profilePictureEditTappedFromUi_then_navigatesWithShowRemove() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		var showRemove: Boolean? = null
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = { shouldShowRemove ->
					showRemove = shouldShowRemove
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			showRemove != null
		}

		assertEquals(true, showRemove)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_removeProfilePictureActionTriggered_then_navigatesToRemoveConfirmationDialog() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		var navigateCalls = 0

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {
					navigateCalls++
				},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		runOnIdle {
			viewModel.removeProfilePictureAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			navigateCalls > 0
		}

		assertEquals(1, navigateCalls)
	}

	@Test
	fun when_initialLoadFailsWithGenericError_then_routeForwardsSnackBarEffect() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel(
			userRepository = RecordingUserRepository(
				users = flow { throw IllegalStateException("boom") }
			)
		)
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
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			shownSnackBars.isNotEmpty()
		}

		assertEquals("¡Ha ocurrido un error!", shownSnackBars.first().message)
		assertEquals(0, outdatedPasswordNavigations)
	}

	@Test
	fun when_loadFailsWithOutdatedPasswordAfterContent_then_routeNavigatesToUpdatePassword() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel(
			userRepository = RecordingUserRepository(
				users = flow {
					emit(DEFAULT_SUMMARY_USER)
					throw clientRequestException(
						statusCode = HttpStatusCode.Conflict,
						path = "/user"
					)
				}
			)
		)
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
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			outdatedPasswordNavigations > 0
		}

		assertEquals(1, outdatedPasswordNavigations)
		assertTrue(shownSnackBars.isEmpty())
	}

	@Test
	fun when_profilePictureSettingsActionTriggeredWithoutProfilePicture_then_navigatesWithoutRemoveOption() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel(
			userRepository = RecordingUserRepository(
				users = flow {
					emit(DEFAULT_SUMMARY_USER.copy(pictureUrl = ""))
				}
			)
		)
		var showRemove: Boolean? = null

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = { shouldShowRemove ->
					showRemove = shouldShowRemove
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
		}

		runOnIdle {
			viewModel.openProfilePictureSettingsAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			showRemove != null
		}

		assertEquals(false, showRemove)
	}

	@Test
	fun when_profilePictureEditTappedFromUiWithoutProfilePicture_then_navigatesWithoutRemoveOption() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel(
			userRepository = RecordingUserRepository(
				users = flow {
					emit(DEFAULT_SUMMARY_USER.copy(pictureUrl = ""))
				}
			)
		)
		var showRemove: Boolean? = null

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = { shouldShowRemove ->
					showRemove = shouldShowRemove
				},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = {},
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is Summary.State.Content
		}

		onNodeWithTag(SummaryUiTags.ProfilePictureEditButton).performClick()

		waitUntil(timeoutMillis = 2_000) {
			showRemove != null
		}

		assertEquals(false, showRemove)
	}

	@Test
	fun when_confirmRemoveProfilePictureActionTriggered_then_showsSnackBarWithoutRouteNavigation() = runTuIndiceUiTest {
		val viewModel = createSummaryViewModel()
		var navigateOutdatedCalls = 0
		var openSettingsCalls = 0
		var openRemoveDialogCalls = 0
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = { navigateOutdatedCalls++ },
				onNavigateToProfilePictureSettingsDialog = { openSettingsCalls++ },
				onNavigateToRemoveProfilePictureConfirmationDialog = { openRemoveDialogCalls++ },
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel
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
		assertEquals(0, openSettingsCalls)
		assertEquals(0, openRemoveDialogCalls)
		assertTrue(shownSnackBars.first().message.isNotBlank())
	}

	@Test
	fun when_retryTappedAfterFailure_then_routeRequestsLoadAgain() = runTuIndiceUiTest {
		val userRepository = CountingFailingUserRepository()
		val viewModel = createSummaryViewModel(userRepository = userRepository)
		val shownSnackBars = mutableListOf<SnackBarMessage>()

		setTuIndiceTestContent {
			SummaryRoute(
				onNavigateToUpdatePassword = {},
				onNavigateToProfilePictureSettingsDialog = {},
				onNavigateToRemoveProfilePictureConfirmationDialog = {},
				showSnackBar = { message ->
					shownSnackBars += message
				},
				viewModel = viewModel
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
