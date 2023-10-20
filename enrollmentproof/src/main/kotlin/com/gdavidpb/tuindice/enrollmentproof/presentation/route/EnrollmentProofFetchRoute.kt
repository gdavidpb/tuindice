package com.gdavidpb.tuindice.enrollmentproof.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectEffectWithLifecycle
import com.gdavidpb.tuindice.base.utils.extension.openFile
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import com.gdavidpb.tuindice.enrollmentproof.ui.dialog.EnrollmentProofFetchDialog
import org.koin.androidx.compose.koinViewModel
import java.io.File

@Composable
fun EnrollmentProofFetchRoute(
	onNavigateToUpdatePassword: () -> Unit,
	onDismissRequest: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	viewModel: EnrollmentProofViewModel = koinViewModel()
) {
	val context = LocalContext.current

	CollectEffectWithLifecycle(flow = viewModel.viewEvent) { event ->
		when (event) {
			is Enrollment.Event.CloseDialog ->
				onDismissRequest()

			is Enrollment.Event.NavigateToOutdatedPassword ->
				onNavigateToUpdatePassword()

			is Enrollment.Event.OpenEnrollmentProof ->
				context.openFile(file = File(event.path))

			is Enrollment.Event.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = event.message))
		}
	}

	LaunchedEffect(Unit) {
		viewModel.fetchEnrollmentProofAction()
	}

	EnrollmentProofFetchDialog(
		onDismissRequest = onDismissRequest
	)
}