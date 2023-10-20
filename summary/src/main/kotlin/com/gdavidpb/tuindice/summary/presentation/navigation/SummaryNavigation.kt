package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.extension.navigatePopUpTo
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute

fun NavController.navigateToSummary() {
	navigatePopUpTo(Destination.Summary)
}

fun NavGraphBuilder.summaryScreen(
	navigateToUpdatePassword: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	composable(Destination.Summary.route) {
		SummaryRoute(
			onNavigateToUpdatePassword = navigateToUpdatePassword,
			showSnackBar = showSnackBar
		)
	}
}