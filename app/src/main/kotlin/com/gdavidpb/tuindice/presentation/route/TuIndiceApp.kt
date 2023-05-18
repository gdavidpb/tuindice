package com.gdavidpb.tuindice.presentation.route

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.about.presentation.navigation.aboutScreen
import com.gdavidpb.tuindice.base.presentation.navigation.browserScreen
import com.gdavidpb.tuindice.base.presentation.navigation.navigateToBrowser
import com.gdavidpb.tuindice.login.presentation.navigation.signInScreen
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.presentation.navigation.recordScreen
import com.gdavidpb.tuindice.summary.presentation.navigation.summaryScreen
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import com.gdavidpb.tuindice.utils.extension.mapBottomRoutes
import org.koin.androidx.compose.koinViewModel

@Composable
fun TuIndiceApp(
	startData: String?,
	viewModel: MainViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

	val navController = rememberNavController()

	LaunchedEffect(Unit) {
		viewModel.startUpAction(data = startData)
	}

	val state = viewState

	if (state is Main.State.Started) {
		LaunchedEffect(navController) {
			navController.currentBackStackEntryFlow
				.mapBottomRoutes(state.destinations)
				.collect { topRoute ->
					viewModel.setLastScreenAction(route = topRoute)
				}
		}

		TuIndiceScreen(
			state = state,
			onNavigateTo = navController::navigate,
			onNavigateBack = navController::popBackStack
		) { innerPadding ->
			NavHost(
				navController = navController,
				startDestination = state.startDestination.route,
				modifier = Modifier.padding(innerPadding)
			) {
				signInScreen()

				summaryScreen()

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