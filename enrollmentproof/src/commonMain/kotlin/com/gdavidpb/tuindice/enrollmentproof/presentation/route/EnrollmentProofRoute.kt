package com.gdavidpb.tuindice.enrollmentproof.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.domain.repository.FileOpenerRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import com.gdavidpb.tuindice.enrollmentproof.ui.dialog.EnrollmentProofContentDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.enrollmentproof.generated.resources.Res
import tuindice.enrollmentproof.generated.resources.enrollment_proof_retry
import tuindice.enrollmentproof.generated.resources.error_enrollment_unsupported

@Composable
fun EnrollmentProofRoute(
	onNavigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	externalActions: FileOpenerRepository,
	viewModel: EnrollmentProofViewModel,
	onRetryRequest: () -> Unit = {}
) {
	val viewState by viewModel.state.collectAsStateWithLifecycle()
	var dismissed by remember { mutableStateOf(false) }
	val proofViewerMissingMessage = stringResource(Res.string.error_enrollment_unsupported)
	val retryActionLabel = stringResource(Res.string.enrollment_proof_retry)

	val dismiss = {
		dismissed = true
		onDismissRequest()
	}

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		if (dismissed) return@CollectEffectWithLifecycle

		when (effect) {
			is Enrollment.Effect.NavigateToOutdatedCredentials ->
				onNavigateToUpdatePassword()

			is Enrollment.Effect.OpenEnrollmentProof -> {
				if (!externalActions.openFile(effect.file)) {
					showSnackBar(SnackBarMessage(message = proofViewerMissingMessage))
				}
				dismiss()
			}

			is Enrollment.Effect.ShowSnackBar -> {
				showSnackBar(
					SnackBarMessage(
						message = effect.message,
						actionLabel = retryActionLabel,
						onAction = onRetryRequest
					)
				)
				dismiss()
			}
		}
	}

	EnrollmentProofContentDialog(
		state = viewState,
		onDismissRequest = dismiss
	)
}
