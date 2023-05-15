package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute
import com.gdavidpb.tuindice.base.ui.route.BrowserRoute
import com.gdavidpb.tuindice.base.utils.extension.encodeUrl
import com.gdavidpb.tuindice.record.ui.screen.RecordScreen
import com.gdavidpb.tuindice.summary.ui.screen.SummaryScreen
import com.gdavidpb.tuindice.ui.navigation.Destination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceApp(destinations: List<Destination>) {
	val appName = stringResource(R.string.app_name)

	val navController = rememberNavController()

	val appTitle = rememberSaveable { mutableStateOf(appName) }
	val appIsTopDestination = rememberSaveable { mutableStateOf(true) }

	LaunchedEffect(navController) {
		navController.currentBackStackEntryFlow.collect { entry ->
			val currentDestination = entry
				.destination
				.route
				?.substringBefore("/")

			val targetDestination = destinations.find { destination ->
				destination.route == currentDestination
			}

			val targetTitle = targetDestination
				?.title
				?.trim('{', '}')
				?.let { title -> entry.arguments?.getString(title, null) }

			if (targetDestination != null) {
				appTitle.value = targetTitle ?: targetDestination.title
				appIsTopDestination.value = targetDestination.topDestination
			}
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = appTitle.value) },
				navigationIcon = {
					if (appIsTopDestination.value) return@TopAppBar

					IconButton(onClick = navController::popBackStack) {
						Icon(
							imageVector = Icons.Filled.ArrowBack,
							contentDescription = null
						)
					}
				}
			)
		},
		bottomBar = {
			if (!appIsTopDestination.value) return@Scaffold

			val backStackEntry = navController.currentBackStackEntryAsState()

			NavigationBar(
				modifier = Modifier.height(dimensionResource(id = R.dimen.dp_48))
			) {
				destinations.forEach { destination ->
					val navigationBarConfig = destination.navigationBarConfig

					if (navigationBarConfig != null) {
						val isSelected =
							(destination.route == backStackEntry.value?.destination?.route)

						NavigationBarItem(
							icon = {
								Icon(
									imageVector = if (isSelected) navigationBarConfig.selectedIcon else navigationBarConfig.unselectedIcon,
									contentDescription = destination.title
								)
							},
							selected = isSelected,
							onClick = {
								navController.navigate(destination.route) {
									val currentRoute = backStackEntry.value?.destination?.route

									if (currentRoute != null)
										popUpTo(currentRoute) {
											inclusive = true
											saveState = true
										}

									launchSingleTop = true
									restoreState = true
								}
							}
						)
					}
				}
			}
		}
	) { innerPadding ->
		NavHost(
			navController = navController,
			startDestination = Destination.Summary.route,
			modifier = Modifier.padding(innerPadding)
		) {
			composable(Destination.Summary.route) {
				SummaryScreen()
			}

			composable(Destination.Record.route) {
				RecordScreen()
			}

			composable(Destination.About.route) {
				AboutRoute(
					onNavigateToBrowser = { title, url ->
						navController.navigate("browser/$title/${url.encodeUrl()}")
					},
					onNavigateToReportBug = { /* TODO */ }
				)
			}

			composable(
				route = "browser/{title}/{url}",
				arguments = listOf(
					navArgument("title") { type = NavType.StringType },
					navArgument("url") { type = NavType.StringType }
				)
			) { backStackEntry ->
				BrowserRoute(
					title = backStackEntry.arguments?.getString("title") ?: "",
					url = backStackEntry.arguments?.getString("url") ?: ""
				)
			}
		}
	}
}
