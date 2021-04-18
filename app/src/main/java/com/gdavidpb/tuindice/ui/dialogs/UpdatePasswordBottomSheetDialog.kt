package com.gdavidpb.tuindice.ui.dialogs

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.utils.Validation
import com.gdavidpb.tuindice.data.utils.`do`
import com.gdavidpb.tuindice.data.utils.`when`
import com.gdavidpb.tuindice.data.utils.firstInvalid
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.errors.UpdatePasswordError
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.UpdatePasswordViewModel
import com.gdavidpb.tuindice.utils.extensions.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_update_password.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdatePasswordBottomSheetDialog : BottomSheetDialogFragment() {

    private val viewModel by viewModel<UpdatePasswordViewModel>()

    private val mainViewModel by sharedViewModel<MainViewModel>()

    private val validations by lazy {
        arrayOf<Validation<*>>(
                `when`(tInputPassword) { isBlank() } `do` { setError(R.string.error_empty) }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_update_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tInputPassword.setAction {
            onConfirmClick()
        }

        btnConfirm.onClickOnce(::onConfirmClick)
        btnCancel.onClickOnce(::onCancelClick)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(viewModel) {
            observe(updatePassword, ::updatePasswordObserver)
        }
    }

    override fun isCancelable(): Boolean {
        return false
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
            val activity = requireActivity() as AppCompatActivity

            activity.hideSoftKeyboard()

            viewModel.updatePassword(password = tInputPassword.getPassword())
        }
    }

    private fun onCancelClick() {
        dismiss()
    }

    private fun updatePasswordObserver(result: Completable<UpdatePasswordError>?) {
        when (result) {
            is Completable.OnLoading -> {
                showLoading(true)
            }
            is Completable.OnComplete -> {
                showLoading(false)

                mainViewModel.trySyncAccount()

                dismiss()
            }
            is Completable.OnError -> {
                showLoading(false)

                updatePasswordErrorHandler(error = result.error)
            }
        }
    }

    private fun updatePasswordErrorHandler(error: UpdatePasswordError?) {
        when (error) {
            is UpdatePasswordError.Timeout -> errorSnackBar(R.string.snack_timeout) { onConfirmClick() }
            is UpdatePasswordError.InvalidCredentials -> errorSnackBar(R.string.snack_invalid_password)
            is UpdatePasswordError.NoConnection -> connectionSnackBar(error.isNetworkAvailable) { onConfirmClick() }
            else -> errorSnackBar { onConfirmClick() }
        }
    }
}