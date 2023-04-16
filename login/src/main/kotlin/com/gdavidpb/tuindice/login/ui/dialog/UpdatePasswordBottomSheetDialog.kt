package com.gdavidpb.tuindice.login.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.login.R
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInError
import com.gdavidpb.tuindice.login.presentation.viewmodel.SignInViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_update_password.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdatePasswordBottomSheetDialog : BottomSheetDialogFragment() {

	private val mainViewModel by viewModel<MainViewModel>()

	private val viewModel by viewModel<SignInViewModel>()

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		return inflater.inflate(R.layout.dialog_update_password, container, false)
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		isCancelable = false

		tInputPassword.setAction(::onConfirmClick)
		btnConfirm.onClickOnce(::onConfirmClick)
		btnCancel.onClickOnce(::dismiss)

		launchRepeatOnLifecycle {
			with(viewModel) {
				collect(signIn, ::signInCollector)
			}
		}
	}

	private fun setLoading(value: Boolean) {
		pBarUpdate.isVisible = value
		tInputPassword.isEnabled = !value
		btnConfirm.isEnabled = !value
		btnConfirm.text = if (value)
			null
		else
			getString(R.string.dialog_button_update_password_confirm)
	}

	private fun setError(@StringRes resource: Int) {
		tInputPassword.setError(resource)
	}

	private fun onConfirmClick() {
		viewModel.reSignIn(password = tInputPassword.getPassword())
	}

	private fun signInCollector(result: UseCaseState<Unit, SignInError>?) {
		when (result) {
			is UseCaseState.Loading -> {
				setLoading(true)
			}
			is UseCaseState.Data -> {
				setLoading(false)

				toast(R.string.toast_password_updated)

				dismiss()
			}
			is UseCaseState.Error -> {
				setLoading(false)

				signInErrorHandler(error = result.error)
			}
			else -> {}
		}
	}

	private fun signInErrorHandler(error: SignInError?) {
		when (error) {
			is SignInError.Timeout -> setError(R.string.snack_timeout)
			is SignInError.InvalidCredentials -> setError(R.string.snack_invalid_credentials)
			is SignInError.Unavailable -> setError(R.string.snack_service_unavailable)
			is SignInError.NoConnection -> setError(
				if (error.isNetworkAvailable)
					R.string.snack_service_unavailable
				else
					R.string.snack_network_unavailable
			)
			is SignInError.AccountDisabled -> mainViewModel.signOut()
			else -> setError(R.string.snack_default_error)
		}
	}
}