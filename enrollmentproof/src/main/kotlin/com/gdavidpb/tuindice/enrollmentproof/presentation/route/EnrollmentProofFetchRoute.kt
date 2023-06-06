package com.gdavidpb.tuindice.enrollmentproof.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
	showSnackBar: (message: String, actionLabel: String?, action: (() -> Unit)?) -> Unit,
	viewModel: EnrollmentProofViewModel = koinViewModel()
) {
	val viewState by viewModel.viewState.collectAsStateWithLifecycle()

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
				showSnackBar(
					event.message,
					null,
					null
				)
		}
	}

	LaunchedEffect(Unit) {
		viewModel.fetchEnrollmentProofAction()
	}

	EnrollmentProofFetchDialog(
		state = viewState,
		onDismissRequest = onDismissRequest
	)
}