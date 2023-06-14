package com.gdavidpb.tuindice.base.presentation.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBarConfig(
	val unselectedIcon: ImageVector,
	val selectedIcon: ImageVector
) {
	object Summary : BottomBarConfig(
		unselectedIcon = Icons.Outlined.BookmarkBorder,
		selectedIcon = Icons.Filled.Bookmark
	)

	object Record : BottomBarConfig(
		unselectedIcon = Icons.Outlined.Book,
		selectedIcon = Icons.Filled.Book
	)

	object Evaluations : BottomBarConfig(
		unselectedIcon = Icons.Outlined.DateRange,
		selectedIcon = Icons.Filled.DateRange
	)

	object About : BottomBarConfig(
		unselectedIcon = Icons.Outlined.FavoriteBorder,
		selectedIcon = Icons.Filled.Favorite
	)
}