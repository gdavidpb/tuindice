package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.enrollmentproof.presentation.route.EnrollmentProofFetchRoute
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.bottomSheet

fun NavController.navigateToEnrollmentProofFetch() {
	navigate(Destination.EnrollmentProofFetch.route)
}

@OptIn(ExperimentalMaterialNavigationApi::class)
fun NavGraphBuilder.enrollmentProofFetchDialog(
	navigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit
) {
	bottomSheet(Destination.EnrollmentProofFetch.route) {
		EnrollmentProofFetchRoute(
			onNavigateToUpdatePassword = navigateToUpdatePassword,
			onDismissRequest = onDismissRequest,
			showSnackBar = showSnackBar
		)
	}
}