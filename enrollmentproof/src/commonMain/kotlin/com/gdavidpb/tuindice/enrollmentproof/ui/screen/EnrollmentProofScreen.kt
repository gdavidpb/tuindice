package com.gdavidpb.tuindice.enrollmentproof.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.ui.view.EnrollmentProofFetchingView

@Composable
fun EnrollmentProofScreen(
	state: Enrollment.State,
	onDismissRequest: () -> Unit
) {
	when (state) {
		is Enrollment.State.Fetching ->
			EnrollmentProofFetchingView(
				onDismissRequest = onDismissRequest
			)
	}
}
