package com.gdavidpb.tuindice.enrollmentproof.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.base.NavigationBaseDirections
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.utils.extension.connectionSnackBar
import com.gdavidpb.tuindice.base.utils.extension.errorSnackBar
import com.gdavidpb.tuindice.base.utils.extension.launchRepeatOnLifecycle
import com.gdavidpb.tuindice.base.utils.extension.navigate
import com.gdavidpb.tuindice.base.utils.extension.openFile
import com.gdavidpb.tuindice.base.utils.extension.snackBar
import com.gdavidpb.tuindice.enrollmentproof.R
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class EnrollmentProofFetchBottomSheetDialog : BottomSheetDialogFragment() {

	private val viewModel by viewModel<EnrollmentProofViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.dialog_enrollment_proof_fetch, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		isCancelable = false

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewState, ::stateCollector)
				collect(viewEvent, ::eventCollector)
			}
		}

		viewModel.fetchEnrollmentProofAction()
	}

	private fun stateCollector(state: Enrollment.State) {
		when (state) {
			Enrollment.State.Fetching -> {}
			Enrollment.State.Fetched -> {}
			Enrollment.State.Failed -> {}
		}
	}

	private fun eventCollector(event: Enrollment.Event) {
		when (event) {
			is Enrollment.Event.CloseDialog -> dismiss()
			is Enrollment.Event.OpenEnrollmentProof -> openFile(file = File(event.path))
			is Enrollment.Event.NavigateToOutdatedPassword -> navigateToOutdatedPassword()
			is Enrollment.Event.ShowUnsupportedFileSnackBar -> snackBar(R.string.snack_enrollment_unsupported)
			is Enrollment.Event.ShowTimeoutSnackBar -> errorSnackBar(R.string.snack_timeout) { viewModel.fetchEnrollmentProofAction() }
			is Enrollment.Event.ShowNotFoundSnackBar -> snackBar(R.string.snack_enrollment_not_found)
			is Enrollment.Event.ShowUnavailableSnackBar -> errorSnackBar(R.string.snack_service_unavailable) { viewModel.fetchEnrollmentProofAction() }
			is Enrollment.Event.ShowNoConnectionSnackBar -> connectionSnackBar(event.isNetworkAvailable) { viewModel.fetchEnrollmentProofAction() }
			is Enrollment.Event.ShowDefaultErrorError -> errorSnackBar { viewModel.fetchEnrollmentProofAction() }
		}
	}

	private fun navigateToOutdatedPassword() {
		navigate(NavigationBaseDirections.navToUpdatePassword())
	}
}