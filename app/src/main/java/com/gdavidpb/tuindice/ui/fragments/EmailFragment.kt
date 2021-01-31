package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Flow
import com.gdavidpb.tuindice.domain.usecase.errors.SendResetPasswordEmailError
import com.gdavidpb.tuindice.domain.usecase.errors.SendVerificationEmailError
import com.gdavidpb.tuindice.presentation.viewmodel.EmailViewModel
import com.gdavidpb.tuindice.utils.FLAG_RESET
import com.gdavidpb.tuindice.utils.FLAG_VERIFY
import com.gdavidpb.tuindice.utils.KEY_TIME_VERIFICATION_COUNT_DOWN
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.fragment_email.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmailFragment : NavigationFragment() {
    private val viewModel by viewModel<EmailViewModel>()

    private val countdownTime by config<Long>(KEY_TIME_VERIFICATION_COUNT_DOWN)

    private val args by navArgs<EmailFragmentArgs>()

    override fun onCreateView() = R.layout.fragment_email

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            observe(countdown, ::countdownObserver)
            observe(signOut, ::signOutObserver)
            observe(verificationEmail, ::sendEmailVerificationObserver)
            observe(resetPasswordEmail, ::resetPasswordObserver)

            startCountdown(time = countdownTime)
        }

        /* Logo animation */
        iViewLogo.animateZoomInOut()

        /* Set up view */
        when (args.awaitingState) {
            FLAG_RESET -> {
                iViewLogo.setImageResource(R.drawable.ic_reset)
                tViewEmailTitle.text = getString(R.string.label_reset)
                tViewEmailMessage.text = getString(R.string.message_reset, args.awaitingEmail)
            }
            FLAG_VERIFY -> {
                iViewLogo.setImageResource(R.drawable.ic_verify)
                tViewEmailTitle.text = getString(R.string.label_verify)
                tViewEmailMessage.text = getString(R.string.message_verify, args.awaitingEmail)
            }
        }

        btnResend.onClickOnce(::onResendClick)
        btnReset.onClickOnce(::onResetClick)
    }

    private fun onResendClick() {
        viewModel.startCountdown(time = countdownTime, reset = true)

        when (args.awaitingState) {
            FLAG_RESET -> viewModel.sendResetPasswordEmail()
            FLAG_VERIFY -> viewModel.sendVerificationEmail()
        }
    }

    private fun onResetClick() {
        viewModel.signOut()
    }

    private fun showLoading(value: Boolean) {
        pBarResend.visibleIf(value)
        btnResend.isEnabled = !value
        btnResend.text = if (value) null else getString(R.string.button_resend)
    }

    private fun resendSnackBar() {
        snackBar {
            messageResource = R.string.snack_bar_resend
        }
    }

    private fun countdownObserver(result: Flow<Long, Nothing>?) {
        when (result) {
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
                EmailFragmentDirections.navToLogin().let(::navigate)
            }
            is Completable.OnError -> {
                clearApplicationUserData()
                EmailFragmentDirections.navToLogin().let(::navigate)
            }
        }
    }

    private fun resetPasswordObserver(result: Completable<SendResetPasswordEmailError>?) {
        when (result) {
            is Completable.OnLoading -> {
                showLoading(true)
            }
            is Completable.OnComplete -> {
                showLoading(false)

                resendSnackBar()
            }
            is Completable.OnError -> {
                showLoading(false)

                resetPasswordErrorHandler(error = result.error)
            }
        }
    }

    private fun sendEmailVerificationObserver(result: Completable<SendVerificationEmailError>?) {
        when (result) {
            is Completable.OnLoading -> {
                showLoading(true)
            }
            is Completable.OnComplete -> {
                showLoading(false)

                resendSnackBar()
            }
            is Completable.OnError -> {
                showLoading(false)

                sendEmailVerificationErrorHandler(error = result.error)
            }
        }
    }

    private fun resetPasswordErrorHandler(error: SendResetPasswordEmailError?) {
        when (error) {
            is SendResetPasswordEmailError.AccountDisabled -> disabledAccountDialog()
            is SendResetPasswordEmailError.NoConnection -> noConnectionSnackBar { onResendClick() }
            else -> defaultErrorSnackBar { onResendClick() }
        }
    }

    private fun sendEmailVerificationErrorHandler(error: SendVerificationEmailError?) {
        when (error) {
            is SendVerificationEmailError.AccountDisabled -> disabledAccountDialog()
            is SendVerificationEmailError.NoConnection -> noConnectionSnackBar { onResendClick() }
            else -> defaultErrorSnackBar { onResendClick() }
        }
    }
}