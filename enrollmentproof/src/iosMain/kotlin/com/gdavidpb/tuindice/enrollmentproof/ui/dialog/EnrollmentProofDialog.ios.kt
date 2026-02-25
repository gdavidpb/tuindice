package com.gdavidpb.tuindice.enrollmentproof.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.ui.view.EnrollmentProofLottieLoadingContent
import org.jetbrains.compose.resources.stringResource
import tuindice.enrollmentproof.generated.resources.*

@Composable
fun EnrollmentProofContentDialog(
	state: Enrollment.State,
	onDismissRequest: () -> Unit
) {
	EnrollmentProofFetchDialog(
		state = state,
		onDismissRequest = onDismissRequest,
		fetchingContent = { dismissRequest ->
			EnrollmentProofFetchingSheet(
				messageText = stringResource(Res.string.dialog_message_enrollment_downloading),
				onDismissRequest = dismissRequest,
				loadingContent = {
					EnrollmentProofLottieLoadingContent()
				}
			)
		}
	)
}
