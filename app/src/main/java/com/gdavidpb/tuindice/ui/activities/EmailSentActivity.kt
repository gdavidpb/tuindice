package com.gdavidpb.tuindice.ui.activities

import android.app.ActivityManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Continuous
import com.gdavidpb.tuindice.presentation.viewmodel.EmailSentViewModel
import com.gdavidpb.tuindice.utils.EXTRA_AWAITING_EMAIL
import com.gdavidpb.tuindice.utils.EXTRA_AWAITING_STATE
import com.gdavidpb.tuindice.utils.FLAG_RESET
import com.gdavidpb.tuindice.utils.FLAG_VERIFY
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.activity_email_sent.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmailSentActivity : AppCompatActivity() {

    private val viewModel by viewModel<EmailSentViewModel>()

    private val activityManager by inject<ActivityManager>()

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

            startCountdown()
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

    private fun onResendClick() {
        btnResend.isEnabled = false

        viewModel.startCountdown(forceRestart = true)

        when (awaitingState) {
            FLAG_RESET -> viewModel.sendResetPasswordEmail()
            FLAG_VERIFY -> viewModel.sendVerificationEmail()
        }
    }

    private fun onResetClick() {
        viewModel.signOut()
    }

    private fun countdownObserver(result: Continuous<Long>?) {
        when (result) {
            is Continuous.OnNext -> {
                tViewCountdown.text = (result.value).toCountdown()
            }
            is Continuous.OnComplete -> {
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
                activityManager.clearApplicationUserData()

                startActivity<LoginActivity>()
                finish()
            }
        }
    }

    private fun resetPasswordObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> showSnackBarResend()
            is Completable.OnError -> showSnackBarException(throwable = result.throwable)
        }
    }

    private fun sendEmailVerificationObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> showSnackBarResend()
            is Completable.OnError -> showSnackBarException(throwable = result.throwable)
        }
    }

    private fun showSnackBarResend() {
        snackBar {
            messageResource = R.string.snack_bar_resend
        }
    }
}
