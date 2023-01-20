package com.gdavidpb.tuindice.enrollmentproof.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.base.NavigationBaseDirections
import com.gdavidpb.tuindice.base.domain.usecase.base.Event
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.base.utils.extensions.*
import com.gdavidpb.tuindice.enrollmentproof.R
import com.gdavidpb.tuindice.enrollmentproof.domain.error.GetEnrollmentError
import com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel.EnrollmentProofViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class EnrollmentDownloadingBottomSheetDialog : BottomSheetDialogFragment() {

	private val mainViewModel by sharedViewModel<MainViewModel>()

	private val viewModel by viewModel<EnrollmentProofViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.dialog_enrollment_downloading, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		isCancelable = false

		with(viewModel) {
			observe(enrollment, ::enrollmentObserver)

			tryFetchEnrollmentProof()
		}
	}

	private fun enrollmentObserver(result: Event<String, GetEnrollmentError>?) {
		when (result) {
			is Event.OnSuccess -> {
				val enrollmentFile = File(result.value)

				openPdf(file = enrollmentFile) { snackBar(R.string.snack_enrollment_unsupported) }

				dismiss()
			}
			is Event.OnError -> {
				enrollmentErrorHandler(error = result.error)

				dismiss()
			}
			else -> {}
		}
	}

	private fun enrollmentErrorHandler(error: GetEnrollmentError?) {
		when (error) {
			is GetEnrollmentError.Timeout -> errorSnackBar(R.string.snack_timeout) { viewModel.tryFetchEnrollmentProof() }
			is GetEnrollmentError.NoConnection -> connectionSnackBar(error.isNetworkAvailable) { viewModel.tryFetchEnrollmentProof() }
			is GetEnrollmentError.NotFound -> snackBar(R.string.snack_enrollment_not_found)
			is GetEnrollmentError.AccountDisabled -> mainViewModel.signOut()
			is GetEnrollmentError.OutdatedPassword -> navigate(NavigationBaseDirections.navToUpdatePassword())
			is GetEnrollmentError.Unavailable -> errorSnackBar(R.string.snack_service_unavailable) { viewModel.tryFetchEnrollmentProof() }
			else -> errorSnackBar { viewModel.tryFetchEnrollmentProof() }
		}
	}
}