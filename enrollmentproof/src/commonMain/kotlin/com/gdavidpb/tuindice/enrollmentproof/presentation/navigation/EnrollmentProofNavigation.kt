package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.enrollmentproof.presentation.route.EnrollmentProofRoute
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import org.koin.compose.koinInject

fun NavGraphBuilder.enrollmentProofNavigation(
	navigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	dialog<EnrollmentProofDestination.EnrollmentProofDialog> {
		val externalActions = koinInject<FileOpenerRepository>()
		val viewModel = koinInject<EnrollmentProofViewModel>()

		EnrollmentProofRoute(
			onNavigateToUpdatePassword = navigateToUpdatePassword,
			onDismissRequest = onDismissRequest,
			showSnackBar = showSnackBar,
			externalActions = externalActions,
			viewModel = viewModel
		)
	}
}
