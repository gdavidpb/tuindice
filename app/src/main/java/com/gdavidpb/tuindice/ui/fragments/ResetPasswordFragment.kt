package com.gdavidpb.tuindice.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Flow
import com.gdavidpb.tuindice.domain.usecase.errors.SendResetPasswordEmailError
import com.gdavidpb.tuindice.presentation.viewmodel.ResetPasswordViewModel
import com.gdavidpb.tuindice.ui.dialogs.disabledAccountFailureDialog
import com.gdavidpb.tuindice.utils.ConfigKeys
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.fragment_reset_password.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResetPasswordFragment : NavigationFragment() {
    private val viewModel by viewModel<ResetPasswordViewModel>()

    private val countdownDuration by config<Long>(ConfigKeys.TIME_VERIFICATION_COUNT_DOWN)

    private val args by navArgs<ResetPasswordFragmentArgs>()

    override fun onCreateView() = R.layout.fragment_reset_password

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        iViewLogo.animateZoomInOut()
        tViewResetPasswordMessage.text = getString(R.string.message_reset_password, args.email)

        btnResend.onClickOnce(::onResendClick)
        btnCancel.onClickOnce(::onResetClick)

        viewModel.startCountdown(duration = countdownDuration)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        with(viewModel) {
            observe(countdown, ::countdownObserver)
            observe(signOut, ::signOutObserver)
            observe(resetPasswordEmail, ::resetPasswordObserver)
        }
    }

    private fun onResendClick() {
        viewModel.sendResetPasswordEmail()
    }

    private fun onResetClick() {
        viewModel.signOut()
    }

    private fun resendSnackBar() {
        snackBar {
            messageResource = R.string.snack_resend
        }
    }

    private fun countdownObserver(result: Flow<Long, Nothing>?) {
        when (result) {
            is Flow.OnStart -> {
                btnResend.isEnabled = false
            }
            is Flow.OnNext -> {
                tViewCountdown.text = (result.value).toCountdown()
            }
            is Flow.OnComplete -> {
                btnResend.isEnabled = true

                tViewCountdown.text = (0L).toCountdown()
            }
        }
    }

    private fun signOutObserver(result: Completable<Nothing>?) {
        when (result) {
            is Completable.OnComplete -> {
                navigate(ResetPasswordFragmentDirections.navToSignIn())
            }
            is Completable.OnError -> {
                requireAppCompatActivity().clearApplicationUserData()
                navigate(ResetPasswordFragmentDirections.navToSignIn())
            }
        }
    }

    private fun resetPasswordObserver(result: Completable<SendResetPasswordEmailError>?) {
        when (result) {
            is Completable.OnLoading -> {
                pBarResend.isVisible = true
                btnResend.isEnabled = false
                btnResend.text = null
            }
            is Completable.OnComplete -> {
                pBarResend.isVisible = false
                btnResend.text = getString(R.string.button_reset_password_resend)

                resendSnackBar()

                viewModel.startCountdown(duration = countdownDuration, reset = true)
            }
            is Completable.OnError -> {
                pBarResend.isVisible = false
                btnResend.isEnabled = true
                btnResend.text = getString(R.string.button_reset_password_resend)

                resetPasswordErrorHandler(error = result.error)
            }
        }
    }

    private fun resetPasswordErrorHandler(error: SendResetPasswordEmailError?) {
        when (error) {
            is SendResetPasswordEmailError.Timeout -> errorSnackBar(R.string.snack_timeout) { onResendClick() }
            is SendResetPasswordEmailError.AccountDisabled -> requireAppCompatActivity().disabledAccountFailureDialog()
            is SendResetPasswordEmailError.NoConnection -> connectionSnackBar(error.isNetworkAvailable) { onResendClick() }
            else -> errorSnackBar { onResendClick() }
        }
    }
}