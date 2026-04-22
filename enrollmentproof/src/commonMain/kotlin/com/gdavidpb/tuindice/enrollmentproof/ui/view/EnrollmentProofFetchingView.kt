package com.gdavidpb.tuindice.enrollmentproof.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.enrollmentproof.ui.dialog.EnrollmentProofFetchingSheet
import org.jetbrains.compose.resources.stringResource
import tuindice.enrollmentproof.generated.resources.Res
import tuindice.enrollmentproof.generated.resources.dialog_button_enrollment_cancel
import tuindice.enrollmentproof.generated.resources.dialog_message_enrollment_downloading

@Composable
fun EnrollmentProofFetchingView(
	onDismissRequest: () -> Unit
) {
	EnrollmentProofFetchingSheet(
		cancelText = stringResource(Res.string.dialog_button_enrollment_cancel),
		messageText = stringResource(Res.string.dialog_message_enrollment_downloading),
		onDismissRequest = onDismissRequest,
		loadingContent = {
			EnrollmentProofLottieLoadingContent()
		}
	)
}
