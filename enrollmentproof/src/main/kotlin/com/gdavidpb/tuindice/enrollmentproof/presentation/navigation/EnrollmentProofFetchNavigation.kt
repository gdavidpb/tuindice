package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.enrollmentproof.presentation.route.EnrollmentProofFetchRoute

fun NavGraphBuilder.enrollmentProofFetchNavigation(
	navigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	dialog<EnrollmentProofFetchDestination.EnrollmentProofFetchDialog> {
		EnrollmentProofFetchRoute(
			onNavigateToUpdatePassword = navigateToUpdatePassword,
			onDismissRequest = onDismissRequest,
			showSnackBar = showSnackBar
		)
	}
}