package com.gdavidpb.tuindice.record.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.record.presentation.route.RecordRoute

fun NavController.navigateToRecord() {
	navigate(Destination.Record.route)
}

fun NavGraphBuilder.recordScreen() {
	composable(Destination.Record.route) {
		RecordRoute()
	}
}