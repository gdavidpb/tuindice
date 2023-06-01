package com.gdavidpb.tuindice.base.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import com.gdavidpb.tuindice.base.presentation.model.BottomBarConfig
import com.gdavidpb.tuindice.base.presentation.model.TopBarActionConfig

sealed class Destination(
	val route: String,
	val title: String = "",
	val isTopDestination: Boolean = false,
	val isBottomDestination: Boolean = false,
	val isDialogDestination: Boolean = false,
	val bottomBarConfig: BottomBarConfig? = null,
	val topBarActionConfig: TopBarActionConfig? = null
) {
	object EnrollmentProofFetch :
		Destination(
			route = "enrollment_proof_fetch",
			isDialogDestination = true
		)

	object UpdatePassword :
		Destination(
			route = "update_password",
			isDialogDestination = true
		)

	object SignIn :
		Destination(
			route = "sign_in",
			title = "TuIndice",
			isTopDestination = true,
			isBottomDestination = false
		)

	object Browser :
		Destination(
			route = "browser",
			title = "{title}",
			isTopDestination = false,
			isBottomDestination = false
		)

	object Summary :
		Destination(
			route = "summary",
			title = "Resumen",
			isTopDestination = true,
			isBottomDestination = true,
			bottomBarConfig = BottomBarConfig(
				unselectedIcon = Icons.Outlined.BookmarkBorder,
				selectedIcon = Icons.Filled.Bookmark
			),
			topBarActionConfig = TopBarActionConfig.Summary
		)

	object Record :
		Destination(
			route = "record",
			title = "Informe Académico",
			isTopDestination = true,
			isBottomDestination = true,
			bottomBarConfig = BottomBarConfig(
				unselectedIcon = Icons.Outlined.Book,
				selectedIcon = Icons.Filled.Book
			)
		)

	object About :
		Destination(
			route = "about",
			title = "Acerca de",
			isTopDestination = true,
			isBottomDestination = true,
			bottomBarConfig = BottomBarConfig(
				unselectedIcon = Icons.Outlined.FavoriteBorder,
				selectedIcon = Icons.Filled.Favorite
			)
		)
}