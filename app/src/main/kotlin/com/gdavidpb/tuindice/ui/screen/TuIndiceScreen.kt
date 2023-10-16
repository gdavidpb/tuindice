package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.about.presentation.navigation.aboutScreen
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.browserScreen
import com.gdavidpb.tuindice.base.presentation.navigation.navigateToBrowser
import com.gdavidpb.tuindice.base.ui.view.TopAppBarActionsView
import com.gdavidpb.tuindice.base.ui.view.TopAppBarAnimatedTitleView
import com.gdavidpb.tuindice.base.utils.extension.mapScreenDestination
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.enrollmentProofFetchDialog
import com.gdavidpb.tuindice.evaluations.presentation.navigation.addEvaluationScreen
import com.gdavidpb.tuindice.evaluations.presentation.navigation.evaluationsScreen
import com.gdavidpb.tuindice.evaluations.presentation.navigation.navigateToAddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.navigation.navigateToEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.navigation.navigateToEvaluations
import com.gdavidpb.tuindice.login.presentation.navigation.navigateToSignIn
import com.gdavidpb.tuindice.login.presentation.navigation.navigateToUpdatePassword
import com.gdavidpb.tuindice.login.presentation.navigation.signInScreen
import com.gdavidpb.tuindice.login.presentation.navigation.signOutDialog
import com.gdavidpb.tuindice.login.presentation.navigation.updatePasswordDialog
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.record.presentation.navigation.recordScreen
import com.gdavidpb.tuindice.summary.presentation.navigation.navigateToSummary
import com.gdavidpb.tuindice.summary.presentation.navigation.summaryScreen
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout

@OptIn(
	ExperimentalMaterial3Api::class,
	ExperimentalMaterialNavigationApi::class
)
@Composable
fun TuIndiceScreen(
	state: Main.State,
	updateState: (Main.State) -> Unit,
	navController: NavHostController,
	bottomSheetNavigator: BottomSheetNavigator,
	snackbarHostState: SnackbarHostState,
	onAction: (action: TopBarAction) -> Unit,
	onNavigateToHome: () -> Unit,
	onNavigateTo: (destination: Destination) -> Unit,
	onNavigateBack: () -> Unit,
	onSetLastScreen: (route: String) -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit
) {
	if (state !is Main.State.Content) return

	LaunchedEffect(navController) {
		navController
			.currentBackStackEntryFlow
			.mapScreenDestination(state.destinations)
			.collect { (title, destination) ->
				if (destination.isBottomDestination)
					onSetLastScreen(destination.route)

				if (destination.isTopDestination)
					onNavigateToHome()

				updateState(
					state.copy(
						title = title,
						currentDestination = destination,
						topBarConfig = destination.topBarConfig
					)
				)
			}
	}

	Scaffold(
		snackbarHost = { SnackbarHost(snackbarHostState) },
		topBar = {
			TopAppBar(
				title = {
					TopAppBarAnimatedTitleView(
						title = state.title
					)
				},
				actions = {
					TopAppBarActionsView(
						topBarConfig = state.topBarConfig,
						onAction = onAction
					)
				},
				navigationIcon = {
					if (!state.currentDestination.isTopDestination)
						IconButton(onClick = onNavigateBack) {
							Icon(
								imageVector = Icons.AutoMirrored.Filled.ArrowBack,
								contentDescription = null
							)
						}
				}
			)
		},
		bottomBar = {
			if (state.currentDestination.isBottomDestination) {
				val bottomDestinations = remember(state.destinations) {
					state.destinations.values.filter { destination -> destination.isBottomDestination }
				}

				NavigationBar(
					modifier = Modifier.height(dimensionResource(id = R.dimen.dp_48)),
					containerColor = MaterialTheme.colorScheme.onSecondary
				) {
					bottomDestinations.forEach { destination ->
						val bottomBarConfig = destination.bottomBarConfig

						requireNotNull(bottomBarConfig)

						val isNavigationBarItemSelected =
							(destination.route == state.currentDestination.route)

						val navigationBarItemIcon =
							if (isNavigationBarItemSelected)
								bottomBarConfig.selectedIcon
							else
								bottomBarConfig.unselectedIcon

						NavigationBarItem(
							icon = {
								Icon(
									imageVector = navigationBarItemIcon,
									contentDescription = null
								)
							},
							colors = NavigationBarItemDefaults.colors(
								indicatorColor = MaterialTheme.colorScheme.secondaryContainer
							),
							selected = isNavigationBarItemSelected,
							onClick = { onNavigateTo(destination) }
						)
					}
				}
			}
		}
	) { innerPadding ->
		ModalBottomSheetLayout(bottomSheetNavigator) {
			NavHost(
				navController = navController,
				startDestination = state.startDestination.route,
				modifier = Modifier.padding(innerPadding)
			) {
				enrollmentProofFetchDialog(
					navigateToUpdatePassword = {
						navController.navigateToUpdatePassword()
					},
					onDismissRequest = {
						navController.popBackStack()
					},
					showSnackBar = showSnackBar
				)

				updatePasswordDialog(
					onDismissRequest = {
						navController.popBackStack()
					},
					showSnackBar = showSnackBar
				)

				signOutDialog(
					navigateToSignIn = {
						navController.navigateToSignIn()
					},
					onDismissRequest = {
						navController.popBackStack()
					}
				)

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
					navigateToUpdatePassword = {
						navController.navigateToUpdatePassword()
					},
					showSnackBar = showSnackBar
				)

				recordScreen(
					navigateToUpdatePassword = {
						navController.navigateToUpdatePassword()
					},
					showSnackBar = showSnackBar
				)

				evaluationsScreen(
					navigateToAddEvaluation = {
						navController.navigateToAddEvaluation()
					},
					navigateToEvaluation = { args ->
						navController.navigateToEvaluation(args)
					},
					showSnackBar = showSnackBar
				)

				addEvaluationScreen(
					navigateToEvaluations = {
						navController.navigateToEvaluations()
					},
					showSnackBar = showSnackBar
				)

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
