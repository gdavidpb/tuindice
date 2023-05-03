package com.gdavidpb.tuindice.login.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.NavigationBaseDirections
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.base.utils.extension.hideSoftKeyboard
import com.gdavidpb.tuindice.base.utils.extension.launchRepeatOnLifecycle
import com.gdavidpb.tuindice.base.utils.extension.navigate
import com.gdavidpb.tuindice.base.utils.extension.toast
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.viewmodel.UpdatePasswordViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_update_password.btnCancel
import kotlinx.android.synthetic.main.dialog_update_password.btnConfirm
import kotlinx.android.synthetic.main.dialog_update_password.pBarUpdate
import kotlinx.android.synthetic.main.dialog_update_password.tInputPassword
import kotlinx.android.synthetic.main.fragment_sign_in.pBarLogging
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdatePasswordBottomSheetDialog : BottomSheetDialogFragment() {

	private val viewModel by viewModel<UpdatePasswordViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return inflater.inflate(R.layout.dialog_update_password, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		isCancelable = false

		tInputPassword.setAction { onConfirmClick() }
		btnConfirm.setOnClickListener { onConfirmClick() }
		btnCancel.setOnClickListener { onCancelClick() }

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(viewState, ::stateCollector)
				collect(viewEvent, ::eventCollector)
			}
		}
	}

	private fun stateCollector(state: UpdatePassword.State) {
		when (state) {
			is UpdatePassword.State.Idle -> showLoading(false)
			is UpdatePassword.State.LoggingIn -> showLoading(true)
			is UpdatePassword.State.LoggedIn -> dismiss()
		}
	}

	private fun eventCollector(event: UpdatePassword.Event) {
		when (event) {
			is UpdatePassword.Event.CloseDialog -> dismiss()
			is UpdatePassword.Event.HideSoftKeyboard -> hideSoftKeyboard()
			is UpdatePassword.Event.NavigateToAccountDisabled -> navigateToAccountDisabled()
			is UpdatePassword.Event.ShowPasswordUpdatedToast -> toast(R.string.toast_password_updated)
			is UpdatePassword.Event.ShowPasswordEmptyError -> tInputPassword.setError(R.string.error_empty)
			is UpdatePassword.Event.ShowDefaultErrorError -> tInputPassword.setError(R.string.snack_default_error)
			is UpdatePassword.Event.ShowInvalidCredentialsError -> tInputPassword.setError(R.string.snack_invalid_credentials)
			is UpdatePassword.Event.ShowNoConnectionError -> tInputPassword.setError(if (event.isNetworkAvailable) R.string.snack_service_unavailable else R.string.snack_network_unavailable)
			is UpdatePassword.Event.ShowTimeoutError -> tInputPassword.setError(R.string.snack_timeout)
			is UpdatePassword.Event.ShowUnavailableError -> tInputPassword.setError(R.string.snack_service_unavailable)
		}
	}

	private fun navigateToAccountDisabled() {
		navigate(NavigationBaseDirections.navToAccountDisabled())
	}

	private fun onConfirmClick() {
		viewModel.signInAction(password = tInputPassword.getPassword())
	}

	private fun onCancelClick() {
		viewModel.cancelAction()
	}

	private fun showLoading(value: Boolean) {
		if (pBarLogging.isVisible == value) return

		pBarUpdate.isVisible = value
		tInputPassword.isEnabled = !value
		btnConfirm.isEnabled = !value
		btnConfirm.text = if (value)
			null
		else
			getString(R.string.dialog_button_update_password_confirm)
	}
}