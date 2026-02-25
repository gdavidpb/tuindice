package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.gdavidpb.tuindice.base.domain.repository.ExternalActions
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.route.EnrollmentProofRoute
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import org.koin.compose.koinInject

fun NavGraphBuilder.enrollmentProofNavigation(
	navigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	enrollmentProofDialogContent: @Composable (
		state: Enrollment.State,
		onDismissRequest: () -> Unit
	) -> Unit
) {
	dialog<EnrollmentProofDestination.EnrollmentProofDialog> {
		val externalActions = koinInject<ExternalActions>()
		val viewModel = koinInject<EnrollmentProofViewModel>()

		EnrollmentProofRoute(
			onNavigateToUpdatePassword = navigateToUpdatePassword,
			onDismissRequest = onDismissRequest,
			showSnackBar = showSnackBar,
			externalActions = externalActions,
			viewModel = viewModel,
			content = enrollmentProofDialogContent
		)
	}
}
