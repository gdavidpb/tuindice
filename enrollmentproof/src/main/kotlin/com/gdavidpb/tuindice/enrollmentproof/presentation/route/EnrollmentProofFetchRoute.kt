package com.gdavidpb.tuindice.enrollmentproof.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
	val viewState by viewModel.state.collectAsStateWithLifecycle()

	val context = LocalContext.current

	CollectEffectWithLifecycle(flow = viewModel.effect) { effect ->
		when (effect) {
			is Enrollment.Effect.CloseDialog ->
				onDismissRequest()

			is Enrollment.Effect.NavigateToOutdatedPassword ->
				onNavigateToUpdatePassword()

			is Enrollment.Effect.OpenEnrollmentProof ->
				context.openFile(file = File(effect.path))

			is Enrollment.Effect.ShowSnackBar ->
				showSnackBar(SnackBarMessage(message = effect.message))
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