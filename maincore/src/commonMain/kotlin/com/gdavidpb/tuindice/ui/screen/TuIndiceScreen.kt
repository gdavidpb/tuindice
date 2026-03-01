package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.base.ui.view.TopAppBarActionsView
import com.gdavidpb.tuindice.base.ui.view.TopAppBarAnimatedTitleView
import com.gdavidpb.tuindice.base.utils.extension.isCurrentDestination
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.BottomBarConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceScreen(
	state: Main.State,
	updateState: (Main.State) -> Unit,
	onRetryStartUp: () -> Unit,
	navController: NavHostController,
	isSwipeBackNavigationEnabled: Boolean = false,
	snackbarHostState: SnackbarHostState,
	onAction: (action: TopBarAction) -> Unit,
	onNavigateTo: (destination: Destination) -> Unit,
	onNavigateBack: () -> Unit,
	onConfirmExitClick: () -> Unit,
	isCameraAvailable: Boolean,
	onNavigateToExternalResource: (url: String) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	when (state) {
		is Main.State.Starting -> {
			Box(modifier = Modifier.fillMaxSize()) {
				CircularProgressIndicator(
					modifier = Modifier.align(Alignment.Center)
				)
			}
			return
		}

		is Main.State.Failed -> {
			ErrorView(
				title = "Inicio",
				message = "No se pudo iniciar la app.",
				retryText = "Reintentar",
				onRetryClick = onRetryStartUp,
				headerContent = { ErrorStateAnimationView() }
			)
			return
		}

		is Main.State.Content -> Unit
	}

	val contentState = state
	val latestContentState = rememberUpdatedState(contentState)

	val onViewStateChanged: (ViewState) -> Unit = { currentViewState ->
		updateState(
			latestContentState.value.copy(
				topBarTitle = currentViewState.topBarTitle,
				topBarConfig = currentViewState.topBarConfig,
				isTopBarVisible = currentViewState.isTopBarVisible,
				isBottomBarVisible = currentViewState.isBottomBarVisible
			)
		)
	}

	val bottomBarConfigs = remember {
		listOf(
			BottomBarConfig.Summary,
			BottomBarConfig.Record,
			BottomBarConfig.Evaluations,
			BottomBarConfig.About
		)
	}

	Scaffold(
		snackbarHost = { SnackbarHost(snackbarHostState) },
		topBar = {
			if (contentState.isTopBarVisible) {
				TopAppBar(
					title = {
						TopAppBarAnimatedTitleView(
							title = contentState.topBarTitle
						)
					},
					actions = {
						TopAppBarActionsView(
							topBarConfig = contentState.topBarConfig,
							onAction = onAction,
							actionIconContent = { action ->
								Icon(
									imageVector = action.getIcon(),
									contentDescription = null
								)
							}
						)
					},
					navigationIcon = {
						val hasPreviousBackStackEntry = (navController.previousBackStackEntry != null)

						if (hasPreviousBackStackEntry)
							IconButton(onClick = onNavigateBack) {
								Icon(
									imageVector = Icons.AutoMirrored.Filled.ArrowBack,
									contentDescription = null
								)
							}
					}
				)
			}
		},
		bottomBar = {
			if (contentState.isBottomBarVisible) {
				NavigationBar(
					modifier = Modifier.height(64.dp),
					containerColor = MaterialTheme.colorScheme.onSecondary
				) {
					bottomBarConfigs.forEach { bottomBarConfig ->
						val isNavigationBarItemSelected = navController
							.isCurrentDestination(destination = bottomBarConfig.destination)

						val navigationBarItemIcon =
							bottomBarIcon(
								config = bottomBarConfig,
								selected = isNavigationBarItemSelected
							)

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
							onClick = { onNavigateTo(bottomBarConfig.destination) }
						)
					}
				}
			}
		}
	) { innerPadding ->
		TuIndiceNavHost(
			navController = navController,
			startDestination = contentState.startDestination,
			isSwipeBackNavigationEnabled = isSwipeBackNavigationEnabled,
			modifier = Modifier.padding(innerPadding),
			onConfirmExitClick = onConfirmExitClick,
			isCameraAvailable = isCameraAvailable,
			onNavigateToExternalResource = onNavigateToExternalResource,
			onViewStateChanged = onViewStateChanged,
			showSnackBar = showSnackBar
		)
	}
}

private fun TopBarAction.getIcon(): ImageVector {
	return when (this) {
		is TopBarAction.SignOutAction ->
			Icons.AutoMirrored.Outlined.Logout
		is TopBarAction.FetchEnrollmentProofAction ->
			Icons.Outlined.FindInPage
	}
}

private fun bottomBarIcon(
	config: BottomBarConfig,
	selected: Boolean
): ImageVector = when (config) {
	BottomBarConfig.Summary ->
		if (selected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
	BottomBarConfig.Record ->
		if (selected) Icons.Filled.Book else Icons.Outlined.Book
	BottomBarConfig.Evaluations ->
		if (selected) Icons.Filled.DateRange else Icons.Outlined.DateRange
	BottomBarConfig.About ->
		if (selected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
}
