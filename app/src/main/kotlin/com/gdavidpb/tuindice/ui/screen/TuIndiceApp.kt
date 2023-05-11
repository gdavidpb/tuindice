package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.about.ui.screen.AboutScreen
import com.gdavidpb.tuindice.record.ui.screen.RecordScreen
import com.gdavidpb.tuindice.summary.ui.screen.SummaryScreen

sealed class Screen(
	val route: String,
	val title: String,
	val unselectedIcon: ImageVector,
	val selectedIcon: ImageVector
) {
	object Summary :
		Screen(
			route = "summary",
			title = "Resumen",
			unselectedIcon = Icons.Outlined.BookmarkBorder,
			selectedIcon = Icons.Filled.Bookmark
		)

	object Record :
		Screen(
			route = "record",
			title = "Informe Académico",
			unselectedIcon = Icons.Outlined.Book,
			selectedIcon = Icons.Filled.Book
		)

	object About :
		Screen(
			route = "about",
			title = "Acerca de",
			unselectedIcon = Icons.Outlined.FavoriteBorder,
			selectedIcon = Icons.Filled.Favorite
		)
}

@Composable
@Preview
fun MainScreenPreview() {
	TuIndiceApp(
		screens = listOf(
			Screen.Summary,
			Screen.Record,
			Screen.About
		)
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceApp(screens: List<Screen>) {
	val appName = stringResource(R.string.app_name)
	val appTitle = remember { mutableStateOf(appName) }

	val navController = rememberNavController().apply {
		addOnDestinationChangedListener { _, destination, _ ->
			val screen = screens.find { screen -> screen.route == destination.route }

			if (screen != null) appTitle.value = screen.title
		}
	}

	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text(text = appTitle.value) }
			)
		},
		bottomBar = {
			val backStackEntry = navController.currentBackStackEntryAsState()

			NavigationBar(
				modifier = Modifier.height(dimensionResource(id = R.dimen.dp_48))
			) {
				screens.forEach { screen ->
					val isSelected = (screen.route == backStackEntry.value?.destination?.route)

					NavigationBarItem(
						icon = {
							Icon(
								imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
								contentDescription = screen.title
							)
						},
						selected = isSelected,
						onClick = {
							navController.navigate(screen.route) {
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
	) { innerPadding ->
		NavHost(
			navController = navController,
			startDestination = Screen.Summary.route,
			modifier = Modifier.padding(innerPadding)
		) {
			composable(Screen.Summary.route) { SummaryScreen() }
			composable(Screen.Record.route) { RecordScreen() }
			composable(Screen.About.route) { AboutScreen() }
		}
	}
}
