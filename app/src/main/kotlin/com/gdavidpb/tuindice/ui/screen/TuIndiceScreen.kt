package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.base.ui.view.TopAppBarActionView
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceScreen(
	state: Main.State.Content,
	onNavigateTo: (route: String) -> Unit,
	onNavigateBack: () -> Unit,
	onSignOutClick: () -> Unit,
	onFetchEnrollmentProofClick: () -> Unit,
	navHost: @Composable (
		PaddingValues,
		showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit
	) -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }

	Scaffold(
		snackbarHost = { SnackbarHost(snackbarHostState) },
		topBar = {
			TopAppBar(
				title = { Text(text = state.title) },
				actions = {
					if (state.topBarActionConfig != null)
						TopAppBarActionView(
							topBarActionConfig = state.topBarActionConfig,
							onSignOutClick = onSignOutClick,
							onFetchEnrollmentProofClick = onFetchEnrollmentProofClick
						)
				},
				navigationIcon = {
					if (!state.currentDestination.isTopDestination)
						IconButton(onClick = onNavigateBack) {
							Icon(
								imageVector = Icons.Filled.ArrowBack,
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
							onClick = { onNavigateTo(destination.route) }
						)
					}
				}
			}
		}
	) { innerPadding ->
		navHost(innerPadding) { message, actionLabel, action ->
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
}
