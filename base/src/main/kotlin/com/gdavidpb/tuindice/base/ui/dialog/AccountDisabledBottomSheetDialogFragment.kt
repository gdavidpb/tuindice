package com.gdavidpb.tuindice.base.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gdavidpb.tuindice.base.NavigationBaseDirections
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.utils.extension.navigate
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_bottom_account_disabled.btnClose

// TODO create AccountDisabledViewModel
class AccountDisabledBottomSheetDialogFragment : BottomSheetDialogFragment() {

	//private val mainViewModel by sharedViewModel<MainViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		return inflater.inflate(R.layout.dialog_bottom_account_disabled, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		isCancelable = false

		btnClose.setOnClickListener { onCloseClick() }

		//mainViewModel.signOutAction()
	}

	private fun onCloseClick() {
		navigate(NavigationBaseDirections.navToSignIn())
		dismiss()
	}
}