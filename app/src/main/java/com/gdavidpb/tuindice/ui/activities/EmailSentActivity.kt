package com.gdavidpb.tuindice.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Continuous
import com.gdavidpb.tuindice.presentation.viewmodel.EmailSentViewModel
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.*
import kotlinx.android.synthetic.main.activity_email_sent.*
import org.jetbrains.anko.longToast
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmailSentActivity : AppCompatActivity() {

    private val viewModel by viewModel<EmailSentViewModel>()

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
    }

    private fun onResendClick() {
        btnResend.isEnabled = false

        viewModel.startCountdown(forceRestart = true)

        when (awaitingState) {
            FLAG_RESET -> viewModel.resetPassword()
            FLAG_VERIFY -> viewModel.sendEmailVerification()
        }
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

    private fun resetPasswordObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> longToast(R.string.toast_resend)
            is Completable.OnError -> longToast(R.string.toast_try_again_later)
        }
    }

    private fun sendEmailVerificationObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> longToast(R.string.toast_resend)
            is Completable.OnError -> longToast(R.string.toast_try_again_later)
        }
    }
}
