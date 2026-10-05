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
import tuindice.enrollmentproof.generated.resources.snack_enrollment_proof_cached

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
	val cachedProofMessage = stringResource(Res.string.snack_enrollment_proof_cached)

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
				} else if (effect.isFromCache) {
					// The saved copy stands in for a fresh one: say so, with nothing to retry.
					showSnackBar(SnackBarMessage(message = cachedProofMessage))
				}
				dismiss()
			}

			is Enrollment.Effect.ShowSnackBar -> {
				showSnackBar(
					SnackBarMessage(
						message = effect.message,
						actionLabel = retryActionLabel.takeIf { effect.canRetry },
						onAction = onRetryRequest.takeIf { effect.canRetry }
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
