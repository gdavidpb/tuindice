package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.utils.extension.isCurrentDestination
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TuIndiceAppHostRoute(
	onConfirmExitClick: () -> Unit,
	isSwipeBackNavigationEnabled: Boolean = false,
	browserRepository: BrowserRepository = koinInject(),
	deviceInfoRepository: DeviceInfoRepository = koinInject(),
	sessionInvalidationRepository: SessionInvalidationRepository = koinInject(),
	syncStatusRepository: SyncStatusRepository = koinInject(),
	reviewRepository: ReviewRepository = koinInject(),
	updateRepository: UpdateRepository = koinInject(),
	viewModel: MainViewModel = koinViewModel<MainViewModel>()
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

	LaunchedEffect(Unit) {
		yield()
		viewModel.requestReviewAction()
	}

	LaunchedEffect(lifecycleOwner) {
		lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
			viewModel.checkUpdateAction()
		}
	}

	LaunchedEffect(lifecycleOwner, sessionInvalidationRepository) {
		lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
			sessionInvalidationRepository.observeSessionInvalidation().collect {
				yield()

				navController.navigate(AuthDestination.NavGraph) {
					launchSingleTop = true
					popUpTo(navController.graph.id) {
						inclusive = true
					}
				}
			}
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
		viewModel = viewModel
	) { state, updateState ->
		val syncStatus by syncStatusRepository
			.observeSyncStatus()
			.collectAsStateWithLifecycle(initialValue = SyncStatus.Healthy)

		LaunchedEffect(syncStatus, state) {
			if (state !is com.gdavidpb.tuindice.presentation.contract.Main.State.Content) return@LaunchedEffect
			if (syncStatus != SyncStatus.OutdatedCredentials) return@LaunchedEffect
			if (state.startDestination == AuthDestination.NavGraph) return@LaunchedEffect

			yield()

			val currentRoute = navController.currentBackStackEntryFlow
				.first()
				.destination
				.route

			if (currentRoute == AuthDestination.UpdatePasswordDialog::class.qualifiedName)
				return@LaunchedEffect

			navController.navigate(AuthDestination.UpdatePasswordDialog) {
				launchSingleTop = true
			}
		}

		TuIndiceScreen(
			state = state,
			updateState = updateState,
			onRetryStartUp = viewModel::startUpAction,
			navController = navController,
			isSwipeBackNavigationEnabled = isSwipeBackNavigationEnabled,
			snackbarHostState = snackbarHostState,
			onAction = { action ->
				when (action) {
					is TopBarAction.SignOutAction ->
						navController.navigate(AuthDestination.SignOutDialog)

					is TopBarAction.FetchEnrollmentProofAction ->
						navController.navigate(EnrollmentProofDestination.EnrollmentProofDialog)
				}
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
			onNavigateBack = { navController.navigateUp() },
			onConfirmExitClick = onConfirmExitClick,
			isCameraAvailable = deviceInfoRepository.hasCamera(),
			onNavigateToExternalResource = browserRepository::open,
			showSnackBar = showSnackBar
		)
	}
}
