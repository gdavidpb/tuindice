package com.gdavidpb.tuindice.enrollmentproof.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.domain.repository.ExternalActionsRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import com.gdavidpb.tuindice.enrollmentproof.ui.screen.EnrollmentProofScreen

@Composable
fun EnrollmentProofRoute(
	onNavigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	externalActions: ExternalActionsRepository,
	viewModel: EnrollmentProofViewModel
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Enrollment.Effect.NavigateToOutdatedPassword ->
				onNavigateToUpdatePassword()

			is Enrollment.Effect.OpenEnrollmentProof -> {
				externalActions.openFile(effect.fileRef)
				onDismissRequest()
			}

			is Enrollment.Effect.ShowSnackBar -> {
				showSnackBar(SnackBarMessage(message = effect.message))
				onDismissRequest()
			}
		}
	}

	EnrollmentProofScreen(
		state = viewState,
		onDismissRequest = onDismissRequest
	)
}
