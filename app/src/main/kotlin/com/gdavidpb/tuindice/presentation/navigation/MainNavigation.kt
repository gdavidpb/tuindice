package com.gdavidpb.tuindice.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.gdavidpb.tuindice.ui.dialog.GooglePlayServicesDialog

fun NavGraphBuilder.mainNavigation(
	onConfirmExitClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	dialog<MainDestination.GooglePlayServicesUnavailableDialog> {
		GooglePlayServicesDialog(
			onConfirmExitClick = onConfirmExitClick,
			onDismissRequest = onDismissRequest
		)
	}
}