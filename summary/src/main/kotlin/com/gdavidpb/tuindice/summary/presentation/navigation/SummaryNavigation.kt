package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.extension.navigateToSingleTop
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute

fun NavController.navigateToSummary() {
	navigateToSingleTop(Destination.Summary.route)
}

fun NavGraphBuilder.summaryScreen(
	navigateToUpdatePassword: () -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit
) {
	composable(Destination.Summary.route) {
		SummaryRoute(
			onNavigateToUpdatePassword = navigateToUpdatePassword,
			showSnackBar = showSnackBar
		)
	}
}