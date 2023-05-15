package com.gdavidpb.tuindice.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder

sealed class Destination(
	val route: String,
	val title: String,
	val topDestination: Boolean,
	val navigationBarConfig: NavigationBarConfig? = null
) {
	object Summary :
		Destination(
			route = "summary",
			title = "Resumen",
			topDestination = true,
			navigationBarConfig = NavigationBarConfig(
				unselectedIcon = Icons.Outlined.BookmarkBorder,
				selectedIcon = Icons.Filled.Bookmark
			)
		)

	object Record :
		Destination(
			route = "record",
			title = "Informe Académico",
			topDestination = true,
			navigationBarConfig = NavigationBarConfig(
				unselectedIcon = Icons.Outlined.Book,
				selectedIcon = Icons.Filled.Book
			)
		)

	object About :
		Destination(
			route = "about",
			title = "Acerca de",
			topDestination = true,
			navigationBarConfig = NavigationBarConfig(
				unselectedIcon = Icons.Outlined.FavoriteBorder,
				selectedIcon = Icons.Filled.Favorite
			)
		)

	object Browser :
		Destination(
			route = "browser",
			title = "{title}",
			topDestination = false
		)
}