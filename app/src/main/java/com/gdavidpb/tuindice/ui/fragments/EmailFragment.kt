package com.gdavidpb.tuindice.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Flow
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
            observe(sendEmailVerification, ::sendEmailVerificationObserver)
            observe(resetPassword, ::resetPasswordObserver)

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

    // TODO
    /*
    override fun handleException(throwable: Throwable): Boolean {
        return super.handleException(throwable) || showSnackBarException(throwable).run { true }
    }
    */

    private fun onResendClick() {
        btnResend.isEnabled = false

        viewModel.startCountdown(time = countdownTime, reset = true)

        when (args.awaitingState) {
            FLAG_RESET -> viewModel.sendResetPasswordEmail()
            FLAG_VERIFY -> viewModel.sendVerificationEmail()
        }
    }

    private fun onResetClick() {
        viewModel.signOut()
    }

    private fun countdownObserver(result: Flow<Long, Any>?) {
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

    private fun signOutObserver(result: Completable<Any>?) {
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

    private fun resetPasswordObserver(result: Completable<Any>?) {
        when (result) {
            is Completable.OnComplete -> showSnackBarResend()
            //TODO is Completable.OnError -> handleException(throwable = result.throwable)
        }
    }

    private fun sendEmailVerificationObserver(result: Completable<Any>?) {
        when (result) {
            is Completable.OnComplete -> showSnackBarResend()
            //TODO is Completable.OnError -> handleException(throwable = result.throwable)
        }
    }

    private fun showSnackBarResend() {
        snackBar {
            messageResource = R.string.snack_bar_resend
        }
    }
}