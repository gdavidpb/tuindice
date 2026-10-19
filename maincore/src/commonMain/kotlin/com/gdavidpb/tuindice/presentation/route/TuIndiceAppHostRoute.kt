@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantAvailability
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantMode
import com.gdavidpb.tuindice.base.domain.repository.AssistantAvailabilityRepository
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.EvaluationsWeekFocusRepository
import com.gdavidpb.tuindice.base.domain.repository.ProfileIdentityRepository
import com.gdavidpb.tuindice.base.domain.repository.RecordAttemptFocusRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.TermReportFocusRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceClock
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.mapper.toCoachmarkSurface
import com.gdavidpb.tuindice.presentation.model.MainShellState
import com.gdavidpb.tuindice.presentation.model.toMainShellState
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceRootMode
import com.gdavidpb.tuindice.presentation.navigation.openAssistant
import com.gdavidpb.tuindice.presentation.navigation.rememberTuIndiceNavigator
import com.gdavidpb.tuindice.presentation.navigation.showEvaluationsWeek
import com.gdavidpb.tuindice.presentation.navigation.showRecordAttempt
import com.gdavidpb.tuindice.presentation.navigation.showTermReport
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsDestination
import com.gdavidpb.tuindice.summary.presentation.model.StatisticsTopBarActionBus
import com.gdavidpb.tuindice.summary.presentation.model.TermReportTopBarActionBus
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import com.gdavidpb.tuindice.wizard.presentation.viewmodel.CoachmarkOverlayViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// The one place the screens are given the clock Koin binds (the system one, or the frozen one of a debug
// build), so no host has to provide it and none can forget to.
@Composable
fun TuIndiceAppHostRoute(
	onConfirmExitClick: () -> Unit,
	browserRepository: BrowserRepository = koinInject(),
	reviewRepository: ReviewRepository = koinInject(),
	updateRepository: UpdateRepository = koinInject(),
	pensumTopBarActionBus: PensumTopBarActionBus = koinInject(),
	statisticsTopBarActionBus: StatisticsTopBarActionBus = koinInject(),
	evaluationsWeekFocusRepository: EvaluationsWeekFocusRepository = koinInject(),
	recordAttemptFocusRepository: RecordAttemptFocusRepository = koinInject(),
	viewModel: MainViewModel = koinViewModel<MainViewModel>(),
	coachmarkOverlayViewModel: CoachmarkOverlayViewModel = koinViewModel<CoachmarkOverlayViewModel>()
) {
	val clock = koinInject<Clock>()
	val assistantAvailabilityRepository = koinInject<AssistantAvailabilityRepository>()
	// Observed before it is read: until the observation delivers, the assistant counts as off.
	val assistantAvailability by remember(assistantAvailabilityRepository) {
		assistantAvailabilityRepository.observeAvailability()
	}.collectAsStateWithLifecycle(
		initialValue = AssistantAvailability(mode = AssistantMode.OFF, serverDisabled = false)
	)
	val profileIdentityRepository = koinInject<ProfileIdentityRepository>()
	// The identity the device already holds (no request), for the avatar of the bar. It is null until a user is stored and
	// again once the session ends, so the avatar of a previous account never stays on screen.
	val profileIdentity by remember(profileIdentityRepository) {
		profileIdentityRepository.observeProfileIdentity()
	}.collectAsStateWithLifecycle(initialValue = null)
	val lifecycleOwner = LocalLifecycleOwner.current
	val coroutineScope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val termReportTopBarActionBus: TermReportTopBarActionBus = koinInject()
	val navigatorHolder = remember { mutableStateOf<TuIndiceNavigator?>(null) }

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

	CompositionLocalProvider(LocalTuIndiceClock provides clock) {
		MainRoute(
			onNavigateToGooglePlayServicesUnavailableDialog = {
				navigatorHolder.value?.push(MainDestination.GooglePlayServicesUnavailableDialog)
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
			onShowSnackBar = { message ->
				showSnackBar(SnackBarMessage(message = message))
			},
			onSessionInvalidated = { message ->
				// The first occasion the interface can show it: the notice can arrive before the content
				// (and its navigation) is on screen, so it waits for it instead of being dropped.
				val navigator = snapshotFlow { navigatorHolder.value }.filterNotNull().first()

				// Already signed out (on the sign-in screen, or on its way): there is nothing to take
				// the user away from, and replacing the stack would throw away what is being typed.
				if (navigator.rootMode == TuIndiceRootMode.SIGNED_IN) navigator.replaceAllForSignIn()

				// No message: the user was already told, or there is nothing to tell.
				if (message != null) showSnackBar(SnackBarMessage(message = message))
			},
			onNavigateToSignOutDialog = { pendingChanges ->
				navigatorHolder.value?.push(
					AuthDestination.SignOutDialog(
						totalCount = pendingChanges.totalCount,
						recordCount = pendingChanges.recordCount,
						evaluationsCount = pendingChanges.evaluationsCount,
						hasFailedMutations = pendingChanges.hasFailedMutations
					)
				)
			},
			viewModel = viewModel
		) { state ->
			// The shell follows the active screen's state and the assistant's availability, so both are read here.
			val latestViewState = remember {
				mutableStateOf<ViewState?>(null)
			}
			val shellState = latestViewState.value
				?.toMainShellState(isAssistantVisible = assistantAvailability.isVisible)
				?: MainShellState()
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
			// Both come decided by the machine, which follows the sync while the content is up.
			val content = state as? Main.State.Content
			val syncStatus = content?.syncStatus ?: SyncStatus.Healthy
			val isUpdatePasswordDismissed = content?.isUpdatePasswordDismissed == true
			val latestIsUpdatePasswordDismissed = rememberUpdatedState(isUpdatePasswordDismissed)
			val isContentAvailable = content != null

			val navigator = if (state is Main.State.Content) {
				rememberTuIndiceNavigator(startKey = state.startDestination)
			} else {
				null
			}

			SideEffect {
				navigatorHolder.value = navigator
			}

			LaunchedEffect(syncStatus) {
				// One app_action per transition INTO a degraded status: the collected enum is
				// snapshot state, so equal re-emissions never restart this effect.
				when (syncStatus) {
					SyncStatus.Unavailable -> viewModel.noteSyncUnavailableAction()
					SyncStatus.Failed -> viewModel.noteSyncFailedAction()
					else -> Unit
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

			LaunchedEffect(syncStatus, navigator, isUpdatePasswordDismissed) {
				if (navigator == null) return@LaunchedEffect
				if (!syncStatus.requiresPassword) return@LaunchedEffect
				if (isUpdatePasswordDismissed) return@LaunchedEffect

				snapshotFlow { navigator.rootMode to navigator.currentKey }
					.first { (rootMode, currentKey) ->
						rootMode == TuIndiceRootMode.SIGNED_IN &&
							currentKey != AuthDestination.UpdatePasswordDialog
					}

				if (latestIsUpdatePasswordDismissed.value) return@LaunchedEffect

				navigator.push(AuthDestination.UpdatePasswordDialog)
			}

			TermReportFocusEffect(navigatorHolder = navigatorHolder)

			TuIndiceScreen(
				state = state,
				shellState = shellState,
				onRetryStartUp = viewModel::startUpAction,
				onUpdateAppClick = viewModel::updateAppAction,
				navigator = navigator,
				snackbarHostState = snackbarHostState,
				profileIdentity = profileIdentity,
				onSignOutRequest = viewModel::requestSignOutAction,
				onAction = { action ->
					when (action) {
						is TopBarAction.AssistantAction ->
							navigator?.openAssistant(latestViewState.value?.assistantContext)

						is TopBarAction.SettingsAction ->
							navigator?.push(AboutDestination.Settings)

						is TopBarAction.FetchEnrollmentProofAction ->
							navigator?.push(EnrollmentProofDestination.EnrollmentProofDialog)

						is TopBarAction.RecordTermSelectionAction ->
							onRecordTermSelection.value?.invoke()

						// One way to open the sheet, whichever tab's bar asked: over the tab on screen.
						is TopBarAction.RecordScheduleAction,
						is TopBarAction.EvaluationsScheduleAction,
						-> navigator?.openSchedule()

						is TopBarAction.SearchPensumAction ->
							navigator?.push(SubjectsDestination.SubjectSearch)

						is TopBarAction.ChangePensumAction ->
							pensumTopBarActionBus.dispatch(action)

						is TopBarAction.StatisticsShareAction ->
							statisticsTopBarActionBus.dispatch(action)

						is TopBarAction.TermReportShareAction ->
							termReportTopBarActionBus.dispatch(action)
					}
				},
				onRecordViewModeChange = onRecordViewModeChange.value,
				onRecordTermSelectionAvailable = { callback ->
					onRecordTermSelection.value = callback
				},
				onBackInterceptorAvailable = { interceptor ->
					backInterceptor.value = interceptor
				},
				// A marked cell of the Schedule: ask for that week of Evaluations and for its evaluation, then
				// close the sheet and go there in one move. The request is state, so the tab applies it when it
				// is ready. The jump does not write the section the app opens on.
				onNavigateToEvaluationsWeek = { weekStart, evaluationId ->
					navigator?.showEvaluationsWeek(
						weekFocus = evaluationsWeekFocusRepository,
						weekStart = weekStart,
						evaluationId = evaluationId
					)
				},
				// An attempt the summary hub points at: ask Registro for it, then go there in one move. Like the
				// week jump, it does not write the section the app opens on.
				onNavigateToRecordAttempt = { attemptId ->
					navigator?.showRecordAttempt(
						attemptFocus = recordAttemptFocusRepository,
						attemptId = attemptId
					)
				},
				onNavigateTo = { section ->
					if (navigator != null && navigator.currentTab != section) {
						viewModel.setLastSectionAction(section)
						navigator.switchTab(section)
					}
				},
				onNavigateBack = {
					if (backInterceptor.value?.invoke() != true) {
						navigator?.pop()
					}
				},
				onConfirmExitClick = onConfirmExitClick,
				onNavigateToExternalResource = browserRepository::open,
				onOutdatedAppDetected = {
					viewModel.showOutdatedAppAction(
						OutdatedAppState(minimumVersionCode = Long.MAX_VALUE)
					)
				},
				onUpdatePasswordDismissRequest = {
					viewModel.dismissUpdatePasswordAction()
					navigator?.pop()
				},
				onRecordViewModeChangeAvailable = { callback ->
					onRecordViewModeChange.value = callback
				},
				coachmarkOverlayState = coachmarkOverlayState,
				onCoachmarkPreviousActionClick = coachmarkOverlayViewModel::previousActionClickAction,
				onCoachmarkPrimaryActionClick = coachmarkOverlayViewModel::primaryActionClickAction,
				onViewStateChanged = { viewState ->
					latestViewState.value = viewState

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
}

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

// A tap on the push of a term report opens the report over the summary, once the student is signed in.
// The request waits until the navigator exists, and it is taken once it is shown.
@Composable
private fun TermReportFocusEffect(
	navigatorHolder: State<TuIndiceNavigator?>,
	focusRepository: TermReportFocusRepository = koinInject()
) {
	val pendingTermReport by remember(focusRepository) { focusRepository.observeRequest() }
		.collectAsStateWithLifecycle(initialValue = null)

	LaunchedEffect(pendingTermReport) {
		val insightId = pendingTermReport ?: return@LaunchedEffect
		val navigator = snapshotFlow { navigatorHolder.value }.filterNotNull().first()

		showPendingTermReport(insightId = insightId, navigator = navigator, focusRepository = focusRepository)
	}
}

// Shows the report of [insightId] once the student is signed in, then takes the request: a request is shown once.
internal suspend fun showPendingTermReport(
	insightId: String,
	navigator: TuIndiceNavigator,
	focusRepository: TermReportFocusRepository
) {
	snapshotFlow { navigator.rootMode }.first { rootMode -> rootMode == TuIndiceRootMode.SIGNED_IN }

	navigator.showTermReport(insightId = insightId)
	focusRepository.consume(insightId)
}
