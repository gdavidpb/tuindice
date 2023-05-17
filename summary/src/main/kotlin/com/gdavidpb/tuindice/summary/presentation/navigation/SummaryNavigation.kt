package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.summary.presentation.route.SummaryRoute

fun NavController.navigateToSummary() {
	navigate(Destination.Summary.route)
}

fun NavGraphBuilder.summaryScreen() {
	composable(Destination.Summary.route) {
		SummaryRoute()
	}
}