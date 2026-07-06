package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.PendingChangesRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.logging.appLogger
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.utils.extension.isCurrentDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.domain.repository.OutdatedAppEventRepository
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.MainShellState
import com.gdavidpb.tuindice.presentation.model.toMainShellState
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsDestination
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import com.gdavidpb.tuindice.wizard.presentation.mapper.toCoachmarkSurface
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.CoachmarkOverlayViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.snack_pending_changes_unavailable
import tuindice.maincore.generated.resources.snack_session_invalidated

private val logger = appLogger(tag = "SignOut")
private val updatePasswordSuppressedRoutes = setOf(
	AuthDestination.NavGraph::class.qualifiedName,
	AuthDestination.SignIn::class.qualifiedName
)

@Composable
fun TuIndiceAppHostRoute(
	onConfirmExitClick: () -> Unit,
	isSwipeBackNavigationEnabled: Boolean = false,
	browserRepository: BrowserRepository = koinInject(),
	deviceInfoRepository: DeviceInfoRepository = koinInject(),
	pendingChangesRepository: PendingChangesRepository = koinInject(),
	sessionInvalidationRepository: SessionInvalidationRepository = koinInject(),
	syncStatusRepository: SyncStatusRepository = koinInject(),
	reviewRepository: ReviewRepository = koinInject(),
	updateRepository: UpdateRepository = koinInject(),
	outdatedAppEventRepository: OutdatedAppEventRepository = koinInject(),
	pensumTopBarActionBus: PensumTopBarActionBus = koinInject(),
	viewModel: MainViewModel = koinViewModel<MainViewModel>(),
	coachmarkOverlayViewModel: CoachmarkOverlayViewModel = koinViewModel<CoachmarkOverlayViewModel>()
) {
	val lifecycleOwner = LocalLifecycleOwner.current
	val navController = rememberNavController()
	val coroutineScope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }

	val showSnackBar: (SnackBarMessage) -> Unit = { message ->
		coroutineScope.launch {
			snackbarHostState.currentSnackbarData?.dismiss()

			val snackBarResult = snackbarHostState.showSnackbar(
				message = message.message,
				actionLabel = message.actionLabel,
				duration = if (message.actionLabel == null)
					SnackbarDuration.Short
				else
					SnackbarDuration.Long
			)

			when (snackBarResult) {
				SnackbarResult.ActionPerformed -> message.onAction?.invoke()
				SnackbarResult.Dismissed -> message.onDismissed?.invoke()
			}
		}
	}
	val dismissSnackBar: () -> Unit = {
		snackbarHostState.currentSnackbarData?.dismiss()
	}
	val pendingChangesUnavailableMessage = stringResource(Res.string.snack_pending_changes_unavailable)
	val sessionInvalidatedMessage = stringResource(Res.string.snack_session_invalidated)

	LaunchedEffect(outdatedAppEventRepository) {
		outdatedAppEventRepository.observeOutdatedApp().collect { state ->
			viewModel.showOutdatedAppAction(state)
		}
	}

	MainRoute(
		onNavigateToGooglePlayServicesUnavailableDialog = {
			navController.navigate(MainDestination.GooglePlayServicesUnavailableDialog)
		},
		onRequestReviewFlow = {
			reviewRepository.launchReview()
		},
		onRequestUpdateFlow = { action ->
			updateRepository.launchUpdate(action = action)
		},
		onOpenUpdateStoreFallback = { result ->
			browserRepository.openUpdateStoreFallback(result)
		},
		viewModel = viewModel
	) { state ->
		val shellState = remember {
			mutableStateOf(MainShellState())
		}
		val isPreparingSignOut = remember {
			mutableStateOf(false)
		}
		val isUpdatePasswordDismissedForOutdatedCredentials = remember {
			mutableStateOf(false)
		}
		val onRecordViewModeChange = remember {
			mutableStateOf<((RecordViewMode) -> Unit)?>(null)
		}
		val onRecordTermSelection = remember {
			mutableStateOf<(() -> Unit)?>(null)
		}
		val backInterceptor = remember {
			mutableStateOf<(() -> Boolean)?>(null)
		}
		val coachmarkOverlayState by coachmarkOverlayViewModel.state.collectAsStateWithLifecycle()
		val coachmarkSurfaceKey = remember {
			mutableStateOf<String?>(null)
		}
		val coachmarkVisitCounter = remember {
			mutableIntStateOf(0)
		}
		val syncStatus by syncStatusRepository
			.observeSyncStatus()
			.collectAsStateWithLifecycle(initialValue = SyncStatus.Healthy)
		val isContentAvailable = state is Main.State.Content
		val isUpdatePasswordDismissed = isUpdatePasswordDismissedForOutdatedCredentials.value

		LaunchedEffect(syncStatus) {
			if (syncStatus != SyncStatus.OutdatedCredentials) {
				isUpdatePasswordDismissedForOutdatedCredentials.value = false
			}
		}

		LaunchedEffect(isContentAvailable) {
			if (!isContentAvailable) return@LaunchedEffect

			yield()
			viewModel.requestReviewAction()
		}

		LaunchedEffect(lifecycleOwner, isContentAvailable) {
			if (!isContentAvailable) return@LaunchedEffect

			lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
				viewModel.checkUpdateAction()
				viewModel.requestSyncAction()
			}
		}

		LaunchedEffect(
			lifecycleOwner,
			sessionInvalidationRepository,
			sessionInvalidatedMessage,
			isContentAvailable
		) {
			if (!isContentAvailable) return@LaunchedEffect

			lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
				sessionInvalidationRepository.observeSessionInvalidation().collect {
					yield()

					val graphId = runCatching { navController.graph.id }
						.getOrElse {
							navController.currentBackStackEntryFlow.first()
							navController.graph.id
						}

					navController.navigate(AuthDestination.NavGraph) {
						launchSingleTop = true
						popUpTo(graphId) {
							inclusive = true
						}
					}
					showSnackBar(
						SnackBarMessage(
							message = sessionInvalidatedMessage
						)
					)
				}
			}
		}

		LaunchedEffect(syncStatus, state, isUpdatePasswordDismissed) {
			if (state !is Main.State.Content) return@LaunchedEffect
			if (syncStatus != SyncStatus.OutdatedCredentials) return@LaunchedEffect
			if (isUpdatePasswordDismissed) return@LaunchedEffect

			navController.currentBackStackEntryFlow
				.first { backStackEntry ->
					backStackEntry.destination.route.canShowUpdatePasswordDialog()
				}

			if (isUpdatePasswordDismissedForOutdatedCredentials.value) return@LaunchedEffect

			navController.navigate(AuthDestination.UpdatePasswordDialog) {
				launchSingleTop = true
			}
		}

		TuIndiceScreen(
			state = state,
			shellState = shellState.value,
			onRetryStartUp = viewModel::startUpAction,
			onUpdateAppClick = viewModel::updateAppAction,
			navController = navController,
			isSwipeBackNavigationEnabled = isSwipeBackNavigationEnabled,
			snackbarHostState = snackbarHostState,
			onAction = { action ->
				when (action) {
					is TopBarAction.SignOutAction ->
						if (!isPreparingSignOut.value) {
							coroutineScope.launch {
								try {
									isPreparingSignOut.value = true
									val pendingChanges = try {
										pendingChangesRepository.getPendingChanges()
									} catch (throwable: Throwable) {
										if (throwable is CancellationException) throw throwable

										logger.e(throwable) {
											"Failed to resolve pending changes before opening sign-out dialog."
										}
										showSnackBar(
											SnackBarMessage(
												message = pendingChangesUnavailableMessage
											)
										)
										return@launch
									}

									navController.navigate(
										AuthDestination.SignOutDialog(
											totalCount = pendingChanges.totalCount,
											recordCount = pendingChanges.recordCount,
											evaluationsCount = pendingChanges.evaluationsCount,
											hasFailedMutations = pendingChanges.hasFailedMutations
										)
									)
								} finally {
									isPreparingSignOut.value = false
								}
							}
						}

					is TopBarAction.FetchEnrollmentProofAction ->
						navController.navigate(EnrollmentProofDestination.EnrollmentProofDialog)

					is TopBarAction.RecordTermSelectionAction ->
						onRecordTermSelection.value?.invoke()

					is TopBarAction.SearchPensumAction ->
						navController.navigate(SubjectsDestination.SubjectSearch)

					is TopBarAction.ChangePensumAction ->
						pensumTopBarActionBus.dispatch(action)
				}
			},
			onRecordViewModeChange = onRecordViewModeChange.value,
			onRecordTermSelectionAvailable = { callback ->
				onRecordTermSelection.value = callback
			},
			onBackInterceptorAvailable = { interceptor ->
				backInterceptor.value = interceptor
			},
			onNavigateTo = { destination ->
				val currentDestination = navController.currentDestination?.parent?.route
				val isNewDestination = !navController.isCurrentDestination(destination)

				if (isNewDestination) {
					viewModel.setLastDestinationAction(destination)

					navController.navigate(destination) {
						launchSingleTop = true

						if (currentDestination != null)
							popUpTo(currentDestination) {
								inclusive = true
							}
					}
				}
			},
			onNavigateBack = {
				if (backInterceptor.value?.invoke() != true) {
					navController.navigateUp()
				}
			},
			onConfirmExitClick = onConfirmExitClick,
			isCameraAvailable = deviceInfoRepository.hasCamera(),
			onNavigateToExternalResource = browserRepository::open,
			onOutdatedAppDetected = {
				viewModel.showOutdatedAppAction(
					OutdatedAppState(minimumVersionCode = Long.MAX_VALUE)
				)
			},
			onUpdatePasswordDismissRequest = {
				if (syncStatus == SyncStatus.OutdatedCredentials) {
					isUpdatePasswordDismissedForOutdatedCredentials.value = true
				}
				navController.navigateUp()
			},
			onRecordViewModeChangeAvailable = { callback ->
				onRecordViewModeChange.value = callback
			},
			coachmarkOverlayState = coachmarkOverlayState,
			onCoachmarkPreviousActionClick = coachmarkOverlayViewModel::previousActionClickAction,
			onCoachmarkPrimaryActionClick = coachmarkOverlayViewModel::primaryActionClickAction,
			onViewStateChanged = { viewState ->
				shellState.value = viewState.toMainShellState()

				val routeKey = viewState::class.qualifiedName
					?: viewState::class.simpleName
					?: viewState.toString()

				if (coachmarkSurfaceKey.value != routeKey) {
					coachmarkSurfaceKey.value = routeKey
					coachmarkVisitCounter.intValue += 1
				}

				coachmarkOverlayViewModel.surfaceChangedAction(
					viewState.toCoachmarkSurface(
						visitKey = "$routeKey:${coachmarkVisitCounter.intValue}"
					)
				)
			},
			showSnackBar = showSnackBar,
			dismissSnackBar = dismissSnackBar
		)
	}
}

private fun String?.canShowUpdatePasswordDialog(): Boolean =
	this != null &&
		this !in updatePasswordSuppressedRoutes &&
		this != AuthDestination.UpdatePasswordDialog::class.qualifiedName

private fun BrowserRepository.openUpdateStoreFallback(
	result: UpdateLaunchResult.OpenStoreFallback
) {
	try {
		open(result.primaryUrl)
	} catch (throwable: Throwable) {
		if (throwable is CancellationException) throw throwable
		open(result.fallbackUrl)
	}
}
