package com.gdavidpb.tuindice.presentation.route

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.about.presentation.navigation.aboutScreen
import com.gdavidpb.tuindice.base.presentation.navigation.browserScreen
import com.gdavidpb.tuindice.base.presentation.navigation.navigateToBrowser
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.findActivity
import com.gdavidpb.tuindice.base.utils.extension.mapDestination
import com.gdavidpb.tuindice.base.utils.extension.navigateToSingleTop
import com.gdavidpb.tuindice.login.presentation.navigation.navigateToSignIn
import com.gdavidpb.tuindice.login.presentation.navigation.signInScreen
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.MainDialog
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.presentation.navigation.recordScreen
import com.gdavidpb.tuindice.summary.presentation.navigation.navigateToSummary
import com.gdavidpb.tuindice.summary.presentation.navigation.summaryScreen
import com.gdavidpb.tuindice.ui.dialog.SignOutConfirmationDialog
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.review.ReviewManager
import org.koin.androidx.compose.get
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceApp(
	startData: String?,
	reviewManager: ReviewManager = get(),
	viewModel: MainViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	val context = LocalContext.current
	val lifecycleOwner = LocalLifecycleOwner.current
	val navController = rememberNavController()
	val sheetState = rememberModalBottomSheetState()
	val dialogState = remember { mutableStateOf<MainDialog?>(null) }

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Main.Event.NavigateToSignIn -> {
				navController.navigateToSignIn()
			}

			is Main.Event.ShowSignOutConfirmationDialog -> {
				dialogState.value = MainDialog.SignOutConfirmation
			}

			is Main.Event.ShowNoServicesDialog -> {
			}

			is Main.Event.ShowReviewDialog -> {
				lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
					reviewManager.launchReview(
						activity = context.findActivity(),
						reviewInfo = event.reviewInfo
					)
				}
			}

			is Main.Event.StartUpdateFlow -> {
			}
		}
	}

	LaunchedEffect(Unit) {
		viewModel.startUpAction(data = startData)
		viewModel.requestReviewAction(reviewManager = reviewManager)
	}

	when (dialogState.value) {
		is MainDialog.SignOutConfirmation ->
			SignOutConfirmationDialog(
				sheetState = sheetState,
				onConfirmSignOutClick = viewModel::confirmSignOutAction,
				onDismissRequest = { dialogState.value = null }
			)

		null -> {}
	}

	when (val state = viewState) {
		is Main.State.Starting -> {}
		is Main.State.Failed -> {}
		is Main.State.Content -> {
			LaunchedEffect(navController) {
				navController
					.currentBackStackEntryFlow
					.mapDestination(state.destinations)
					.collect { (title, destination) ->
						if (destination.isBottomDestination)
							viewModel.setLastScreenAction(route = destination.route)

						viewModel.setState(
							state.copy(
								title = title,
								currentDestination = destination,
								topBarActionConfig = destination.topBarActionConfig
							)
						)
					}
			}

			TuIndiceScreen(
				state = state,
				onNavigateTo = navController::navigateToSingleTop,
				onNavigateBack = navController::popBackStack,
				onSignOutClick = viewModel::signOutAction
			) { innerPadding, showSnackBar ->
				NavHost(
					navController = navController,
					startDestination = state.startDestination.route,
					modifier = Modifier.padding(innerPadding)
				) {
					signInScreen(
						navigateToSummary = {
							navController.navigateToSummary()
						},
						navigateToBrowser = { args ->
							navController.navigateToBrowser(args)
						},
						showSnackBar = showSnackBar
					)

					summaryScreen(
						showSnackBar = showSnackBar
					)

					recordScreen()

					aboutScreen(
						navigateToBrowser = { args ->
							navController.navigateToBrowser(args)
						}
					)

					browserScreen()
				}
			}
		}
	}
}