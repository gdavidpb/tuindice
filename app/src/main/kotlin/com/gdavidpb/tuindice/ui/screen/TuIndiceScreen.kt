package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.navigation.BottomSheetNavigator
import androidx.compose.material.navigation.ModalBottomSheetLayout
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.about.presentation.navigation.aboutScreen
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.ui.view.TopAppBarActionsView
import com.gdavidpb.tuindice.base.ui.view.TopAppBarAnimatedTitleView
import com.gdavidpb.tuindice.base.utils.extension.mapDestination
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.enrollmentProofFetchDialog
import com.gdavidpb.tuindice.evaluations.presentation.navigation.evaluationsNavigation
import com.gdavidpb.tuindice.login.presentation.navigation.navigateToSignIn
import com.gdavidpb.tuindice.login.presentation.navigation.navigateToUpdatePassword
import com.gdavidpb.tuindice.login.presentation.navigation.signInScreen
import com.gdavidpb.tuindice.login.presentation.navigation.signOutDialog
import com.gdavidpb.tuindice.login.presentation.navigation.updatePasswordDialog
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.browserNavigation
import com.gdavidpb.tuindice.presentation.navigation.mainNavigation
import com.gdavidpb.tuindice.record.presentation.navigation.recordScreen
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.summaryNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceScreen(
	state: Main.State,
	updateState: (Main.State) -> Unit,
	navController: NavHostController,
	bottomSheetNavigator: BottomSheetNavigator,
	snackbarHostState: SnackbarHostState,
	onAction: (action: TopBarAction) -> Unit,
	onNavigateTo: (destination: Destination) -> Unit,
	onNavigateBack: () -> Unit,
	onSetDestinationScreen: (destination: Destination) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	if (state !is Main.State.Content) return

	LaunchedEffect(navController) {
		navController
			.currentBackStackEntryFlow
			.mapDestination()
			.collect { destination ->
				if (destination.isBottomDestination)
					onSetDestinationScreen(destination)

				updateState(
					state.copy(
						title = destination.title,
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
				NavigationBar(
					modifier = Modifier.height(dimensionResource(id = R.dimen.dp_48)),
					containerColor = MaterialTheme.colorScheme.onSecondary
				) {
					listOf(
						Destination.Record,
						Destination.Evaluations,
						Destination.About
					).forEach { destination ->
						val bottomBarConfig = destination.bottomBarConfig

						requireNotNull(bottomBarConfig)

						val isNavigationBarItemSelected =
							(destination == state.currentDestination)

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
				startDestination = state.startDestination,
				modifier = Modifier.padding(innerPadding)
			) {
				mainNavigation(
					navController = navController
				)

				summaryNavigation(
					navController = navController,
					showSnackBar = showSnackBar
				)

				evaluationsNavigation(
					navController = navController,
					showSnackBar = showSnackBar
				)

				browserNavigation(
					navController = navController
				)

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
					},
					showSnackBar = showSnackBar
				)

				signInScreen(
					navigateToSummary = {
						navController.navigate(SummaryDestination.NavGraph)
					},
					navigateToBrowser = { title, url ->
						navController.navigate(
							BrowserDestination.Browser(
								url = url
							)
						)
					},
					showSnackBar = showSnackBar
				)

				recordScreen(
					navigateToUpdatePassword = {
						navController.navigateToUpdatePassword()
					},
					showSnackBar = showSnackBar
				)

				aboutScreen(
					navigateToBrowser = { title, url ->
						navController.navigate(
							BrowserDestination.Browser(
								url = url
							)
						)
					}
				)
			}
		}
	}
}
