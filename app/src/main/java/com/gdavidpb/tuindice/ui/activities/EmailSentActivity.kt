package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Flow
import com.gdavidpb.tuindice.presentation.viewmodel.EmailSentViewModel
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.activity_email_sent.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmailSentActivity : NavigationActivity(0) {

    private val viewModel by viewModel<EmailSentViewModel>()

    private val countdownTime by config<Long>(KEY_TIME_VERIFICATION_COUNT_DOWN)

    private val awaitingState by lazy {
        intent.getIntExtra(EXTRA_AWAITING_STATE, 0)
    }

    private val awaitingEmail by lazy {
        intent.getStringExtra(EXTRA_AWAITING_EMAIL)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_sent)

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
        when (awaitingState) {
            FLAG_RESET -> {
                iViewLogo.setImageResource(R.drawable.ic_reset)
                tViewEmailTitle.text = getString(R.string.label_reset)
                tViewEmailMessage.text = getString(R.string.message_reset, awaitingEmail)
            }
            FLAG_VERIFY -> {
                iViewLogo.setImageResource(R.drawable.ic_verify)
                tViewEmailTitle.text = getString(R.string.label_verify)
                tViewEmailMessage.text = getString(R.string.message_verify, awaitingEmail)
            }
        }

        btnResend.onClickOnce(::onResendClick)
        btnReset.onClickOnce(::onResetClick)
    }

    override fun handleException(throwable: Throwable): Boolean {
        return super.handleException(throwable) || showSnackBarException(throwable).run { true }
    }

    private fun onResendClick() {
        btnResend.isEnabled = false

        viewModel.startCountdown(time = countdownTime, reset = true)

        when (awaitingState) {
            FLAG_RESET -> viewModel.sendResetPasswordEmail()
            FLAG_VERIFY -> viewModel.sendVerificationEmail()
        }
    }

    private fun onResetClick() {
        viewModel.signOut()
    }

    private fun countdownObserver(result: Flow<Long>?) {
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

    private fun signOutObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> {
                startActivity<LoginActivity>()
                finish()
            }
            is Completable.OnError -> {
                clearApplicationUserData()

                startActivity<LoginActivity>()
                finish()
            }
        }
    }

    private fun resetPasswordObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> showSnackBarResend()
            is Completable.OnError -> handleException(throwable = result.throwable)
        }
    }

    private fun sendEmailVerificationObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> showSnackBarResend()
            is Completable.OnError -> handleException(throwable = result.throwable)
        }
    }

    private fun showSnackBarResend() {
        snackBar {
            messageResource = R.string.snack_bar_resend
        }
    }
}
