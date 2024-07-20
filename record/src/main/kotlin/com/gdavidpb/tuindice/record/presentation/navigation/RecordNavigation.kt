package com.gdavidpb.tuindice.record.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.record.presentation.route.RecordRoute

fun NavGraphBuilder.recordNavigation(
	navController: NavController,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<RecordDestination.NavGraph>(startDestination = RecordDestination.Record) {
		composable<RecordDestination.Record> {
			RecordRoute(
				onNavigateToUpdatePassword = {
					TODO()
				},
				showSnackBar = showSnackBar
			)
		}
	}
}