package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.gdavidpb.tuindice.base.utils.extension.findActivity
import com.gdavidpb.tuindice.ui.dialog.GooglePlayServicesDialog

fun NavGraphBuilder.mainNavigation(
	navController: NavController
) {
	dialog<MainDestination.GooglePlayServicesUnavailableDialog> {
		val context = LocalContext.current

		GooglePlayServicesDialog(
			onConfirmExitClick = {
				context.findActivity().finish()
			},
			onDismissRequest = {
				navController.navigateUp()
			}
		)
	}
}