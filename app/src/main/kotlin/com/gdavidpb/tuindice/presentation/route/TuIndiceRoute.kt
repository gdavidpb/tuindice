package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.gdavidpb.tuindice.base.utils.extension.getViewModel
import com.gdavidpb.tuindice.base.utils.extension.navigateToSingleTop
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.navigateToEnrollmentProofFetch
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.MainDialog
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.ui.dialog.GooglePlayServicesDialog
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewManager
import kotlinx.coroutines.launch
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceRoute(
	startData: String?,
	reviewManager: ReviewManager = get(),
	appUpdateManager: AppUpdateManager = get(),
	viewModel: MainViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current
	val navController = rememberNavController()
	val coroutineScope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }
	val dialogState = rememberDialogState<MainDialog>()

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Main.Event.StartUpdateFlow ->
				appUpdateManager.startUpdateFlowForResult(
					event.updateInfo,
					AppUpdateType.IMMEDIATE,
					context.findActivity(),
					RequestCodes.APP_UPDATE
				)

			is Main.Event.ShowNoServicesDialog ->
				dialogState.value = MainDialog.GooglePlayServicesUnavailable

			is Main.Event.ShowReviewDialog ->
				lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
					reviewManager.launchReview(
						activity = context.findActivity(),
						reviewInfo = event.reviewInfo
					)
				}

			is Main.Event.CloseDialog ->
				dialogState.value = null
		}
	}

	LaunchedEffect(Unit) {
		viewModel.startUpAction(data = startData)
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
		updateState = viewModel::setState,
		navController = navController,
		snackbarHostState = snackbarHostState,
		onAction = { action ->
			when (action) {
				is TopBarAction.SignOutAction ->
					navController.getViewModel<SummaryViewModel>()
						?.signOutAction()

				is TopBarAction.FetchEnrollmentProofAction ->
					navController.navigateToEnrollmentProofFetch()

				is TopBarAction.FilterEvaluationsAction ->
					navController.getViewModel<EvaluationsViewModel>()
						?.openEvaluationsFilters()
			}
		},
		onNavigateTo = navController::navigateToSingleTop,
		onNavigateBack = navController::popBackStack,
		onSetLastScreen = viewModel::setLastScreenAction
	) { message, actionLabel, action ->
		coroutineScope.launch {
			snackbarHostState.currentSnackbarData?.dismiss()

			val snackBarResult = snackbarHostState.showSnackbar(
				message = message,
				actionLabel = actionLabel
			)

			if (snackBarResult == SnackbarResult.ActionPerformed)
				action?.invoke()
		}
	}
}