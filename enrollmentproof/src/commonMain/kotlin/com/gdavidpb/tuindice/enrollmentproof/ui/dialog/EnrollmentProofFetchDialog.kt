package com.gdavidpb.tuindice.enrollmentproof.ui.dialog

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment

@Composable
fun EnrollmentProofFetchDialog(
	state: Enrollment.State,
	onDismissRequest: () -> Unit,
	fetchingContent: @Composable (onDismissRequest: () -> Unit) -> Unit
) {
	if (state != Enrollment.State.Fetching) return

	fetchingContent(onDismissRequest)
}
