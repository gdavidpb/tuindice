package com.gdavidpb.tuindice.presentation.model

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
import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination

sealed class BottomBarConfig(
	val unselectedIcon: ImageVector,
	val selectedIcon: ImageVector,
	val destination: Destination
) {
	data object Summary : BottomBarConfig(
		unselectedIcon = Icons.Outlined.BookmarkBorder,
		selectedIcon = Icons.Filled.Bookmark,
		destination = SummaryDestination.NavGraph
	)

	data object Record : BottomBarConfig(
		unselectedIcon = Icons.Outlined.Book,
		selectedIcon = Icons.Filled.Book,
		destination = RecordDestination.NavGraph
	)

	data object Evaluations : BottomBarConfig(
		unselectedIcon = Icons.Outlined.DateRange,
		selectedIcon = Icons.Filled.DateRange,
		destination = EvaluationsDestination.NavGraph
	)

	data object About : BottomBarConfig(
		unselectedIcon = Icons.Outlined.FavoriteBorder,
		selectedIcon = Icons.Filled.Favorite,
		destination = AboutDestination.NavGraph
	)
}