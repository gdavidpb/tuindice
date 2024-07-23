package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material.navigation.rememberBottomSheetNavigator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.utils.RequestCodes
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.findActivity
import com.gdavidpb.tuindice.base.utils.extension.isCurrentDestination
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofFetchDestination
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun TuIndiceRoute(
	reviewManager: ReviewManager = koinInject(),
	appUpdateManager: AppUpdateManager = koinInject(),
	viewModel: MainViewModel = koinViewModel()
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current

	val bottomSheetNavigator = rememberBottomSheetNavigator()
	val navController = rememberNavController(bottomSheetNavigator)
	val coroutineScope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Main.Effect.StartUpdateFlow ->
				appUpdateManager.startUpdateFlowForResult(
					effect.updateInfo,
					AppUpdateType.IMMEDIATE,
					context.findActivity(),
					RequestCodes.APP_UPDATE
				)

			is Main.Effect.NavigateToGooglePlayServicesUnavailableDialog ->
				navController.navigate(MainDestination.GooglePlayServicesUnavailableDialog)

			is Main.Effect.NavigateToReviewDialog ->
				lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
					reviewManager.launchReview(
						activity = context.findActivity(),
						reviewInfo = effect.reviewInfo
					)
				}
		}
	}

	LaunchedEffect(Unit) {
		viewModel.requestReviewAction(reviewManager = reviewManager)

		lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
			viewModel.checkUpdateAction(appUpdateManager = appUpdateManager)
		}
	}

	TuIndiceScreen(
		state = viewState,
		updateState = viewModel::updateStateAction,
		navController = navController,
		bottomSheetNavigator = bottomSheetNavigator,
		snackbarHostState = snackbarHostState,
		onAction = { action ->
			when (action) {
				is TopBarAction.SignOutAction ->
					navController.navigate(
						LoginDestination.SignOutDialog
					)

				is TopBarAction.FetchEnrollmentProofAction ->
					navController.navigate(
						EnrollmentProofFetchDestination.EnrollmentProofFetchDialog
					)
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
		onNavigateBack = {
			navController.navigateUp()
		}
	) { (message, actionLabel, onAction, onDismissed) ->
		coroutineScope.launch {
			snackbarHostState.currentSnackbarData?.dismiss()

			val snackBarResult = snackbarHostState.showSnackbar(
				message = message,
				actionLabel = actionLabel,
				duration = if (actionLabel == null)
					SnackbarDuration.Short
				else
					SnackbarDuration.Long
			)

			when (snackBarResult) {
				SnackbarResult.ActionPerformed -> onAction?.invoke()
				SnackbarResult.Dismissed -> onDismissed?.invoke()
			}
		}
	}
}