package com.gdavidpb.tuindice.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Event
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.SignInViewModel
import com.gdavidpb.tuindice.utils.Validation
import com.gdavidpb.tuindice.utils.`do`
import com.gdavidpb.tuindice.utils.`when`
import com.gdavidpb.tuindice.utils.extensions.*
import com.gdavidpb.tuindice.utils.firstInvalid
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_update_password.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdatePasswordBottomSheetDialog : BottomSheetDialogFragment() {

    private val viewModel by viewModel<SignInViewModel>()

    private val mainViewModel by sharedViewModel<MainViewModel>()

    private val inputMethodManager by inject<InputMethodManager>()

    private val validations by lazy {
        arrayOf<Validation<*>>(
                `when`(tInputPassword) { isEmpty() } `do` { setError(R.string.error_empty) }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_update_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tInputPassword.setAction {
            onConfirmClick()
        }

        isCancelable = false

        btnConfirm.onClickOnce(::onConfirmClick)
        btnCancel.onClickOnce(::onCancelClick)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(viewModel) {
            observe(signIn, ::signInObserver)
        }
    }

    private fun showLoading(value: Boolean) {
        pBarUpdate.isVisible = value
        tInputPassword.isEnabled = !value
        btnConfirm.isEnabled = !value
        btnConfirm.text = if (value) null else getString(R.string.dialog_button_update_password_confirm)
    }

    private fun onConfirmClick() {
        validations.firstInvalid {
            when (this) {
                is View -> {
                    requestFocus()

                    animateLookAtMe()
                }
            }
        }.isNull {
            hideSoftKeyboard(inputMethodManager)

            viewModel.reSignIn(password = tInputPassword.getPassword())
        }
    }

    private fun onCancelClick() {
        dismiss()
    }

    private fun signInObserver(result: Event<Boolean, SignInError>?) {
        when (result) {
            is Event.OnLoading -> {
                showLoading(true)
            }
            is Event.OnSuccess -> {
                showLoading(false)

                mainViewModel.sync()

                toast(R.string.toast_password_updated)

                dismiss()
            }
            is Event.OnError -> {
                showLoading(false)

                signInErrorHandler(error = result.error)
            }
            else -> {
                showLoading(false)

                tInputPassword.setError(R.string.snack_default_error)
            }
        }
    }

    private fun signInErrorHandler(error: SignInError?) {
        when (error) {
            is SignInError.Timeout -> tInputPassword.setError(R.string.snack_timeout)
            is SignInError.InvalidCredentials -> tInputPassword.setError(R.string.snack_invalid_credentials)
            is SignInError.Unavailable -> tInputPassword.setError(R.string.snack_service_unavailable)
            is SignInError.NoConnection -> tInputPassword.setError(
                if (error.isNetworkAvailable)
                    R.string.snack_service_unavailable
                else
                    R.string.snack_network_unavailable
            )
            is SignInError.AccountDisabled -> mainViewModel.signOut()
            else -> tInputPassword.setError(R.string.snack_default_error)
        }
    }
}