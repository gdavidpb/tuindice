package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.rememberDialogState
import com.gdavidpb.tuindice.base.utils.RequestCodes
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.findActivity
import com.gdavidpb.tuindice.base.utils.extension.navigatePopUpTo
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.navigateToEnrollmentProofFetch
import com.gdavidpb.tuindice.login.presentation.navigation.navigateToSignOut
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.MainDialog
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.ui.dialog.GooglePlayServicesDialog
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialNavigationApi::class)
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
	val dialogState = rememberDialogState<MainDialog>()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Main.Effect.StartUpdateFlow ->
				appUpdateManager.startUpdateFlowForResult(
					effect.updateInfo,
					AppUpdateType.IMMEDIATE,
					context.findActivity(),
					RequestCodes.APP_UPDATE
				)

			is Main.Effect.ShowNoServicesDialog ->
				dialogState.value = MainDialog.GooglePlayServicesUnavailable

			is Main.Effect.ShowReviewDialog ->
				lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
					reviewManager.launchReview(
						activity = context.findActivity(),
						reviewInfo = effect.reviewInfo
					)
				}

			is Main.Effect.CloseDialog ->
				dialogState.value = null
		}
	}

	LaunchedEffect(Unit) {
		viewModel.requestReviewAction(reviewManager = reviewManager)

		lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
			viewModel.checkUpdateAction(appUpdateManager = appUpdateManager)
		}
	}

	when (dialogState.value) {
		is MainDialog.GooglePlayServicesUnavailable -> {
			val nonDismissSheetState = rememberModalBottomSheetState(
				confirmValueChange = { false }
			)

			GooglePlayServicesDialog(
				sheetState = nonDismissSheetState,
				onConfirmExitClick = { context.findActivity().finish() },
				onDismissRequest = viewModel::closeDialogAction
			)
		}

		null -> {}
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
					navController.navigateToSignOut()

				is TopBarAction.FetchEnrollmentProofAction ->
					navController.navigateToEnrollmentProofFetch()
			}
		},
		onNavigateTo = navController::navigatePopUpTo,
		onNavigateBack = navController::popBackStack,
		onSetLastScreen = viewModel::setLastScreenAction
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