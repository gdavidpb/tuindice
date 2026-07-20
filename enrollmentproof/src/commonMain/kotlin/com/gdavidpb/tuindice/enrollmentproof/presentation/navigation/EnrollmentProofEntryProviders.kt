package com.gdavidpb.tuindice.enrollmentproof.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository
import com.gdavidpb.tuindice.base.presentation.navigation.NavShellBindings
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.presentation.navigation.dialogMetadata
import com.gdavidpb.tuindice.enrollmentproof.presentation.route.EnrollmentProofRoute
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun EntryProviderScope<NavKey>.enrollmentProofEntries(
	navActions: TuIndiceNavActions,
	shellBindings: NavShellBindings,
	onNavigateToUpdatePassword: () -> Unit,
	onRetryRequest: () -> Unit
) {
	entry<EnrollmentProofDestination.EnrollmentProofDialog>(metadata = dialogMetadata()) {
		val externalActions = koinInject<FileOpenerRepository>()
		val viewModel = koinViewModel<EnrollmentProofViewModel>()

		EnrollmentProofRoute(
			onNavigateToUpdatePassword = onNavigateToUpdatePassword,
			onDismissRequest = { navActions.pop() },
			onRetryRequest = onRetryRequest,
			showSnackBar = shellBindings.showSnackBar,
			externalActions = externalActions,
			viewModel = viewModel
		)
	}
}
