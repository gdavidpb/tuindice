package com.gdavidpb.tuindice.record.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.record.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EnrollmentDownloadingBottomSheetDialog : BottomSheetDialogFragment() {
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
	}
}