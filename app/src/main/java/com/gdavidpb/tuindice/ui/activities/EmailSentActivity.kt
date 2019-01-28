package com.gdavidpb.tuindice.ui.activities

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Continuous
import com.gdavidpb.tuindice.presentation.viewmodel.EmailSentActivityViewModel
import com.gdavidpb.tuindice.utils.*
import kotlinx.android.synthetic.main.activity_email_sent.*
import org.jetbrains.anko.longToast
import org.koin.android.ext.android.inject
import java.util.concurrent.TimeUnit

class EmailSentActivity : AppCompatActivity() {

    private val viewModel: EmailSentActivityViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_sent)

        with(viewModel) {
            observe(countdown, ::countdownObserver)
            observe(sendEmailVerification, ::sendEmailVerificationObserver)
            observe(resetPassword, ::resetPasswordObserver)

            countdown()
        }

        /* Logo animation */
        val animator = ValueAnimator.ofFloat(.85f, 1f)

        animator.animate({
            duration = 1000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateInterpolator()
        }, {
            val scale = animatedValue as Float

            iViewLogo.scaleX = scale
            iViewLogo.scaleY = scale
        })

        /* Set up view */
        val email = intent.getStringExtra(AWAITING_EMAIL)

        when (intent.getIntExtra(AWAITING_STATE, 0)) {
            FLAG_RESET -> {
                iViewLogo.setImageResource(R.drawable.ic_reset)
                tViewEmailTitle.text = getString(R.string.titleReset)
                tViewEmailMessage.text = getString(R.string.messageReset, email)
            }
            FLAG_VERIFY -> {
                iViewLogo.setImageResource(R.drawable.ic_verify)
                tViewEmailTitle.text = getString(R.string.titleVerify)
                tViewEmailMessage.text = getString(R.string.messageVerify, email)
            }
        }

        btnResend.onClickOnce(::onResendClick)
    }

    private fun onResendClick() {
        btnResend.isEnabled = false

        when (intent.getIntExtra(AWAITING_STATE, 0)) {
            FLAG_RESET -> viewModel.resetPassword()
            FLAG_VERIFY -> viewModel.sendEmailVerification()
        }
    }

    private fun countdownObserver(result: Continuous<Long>?) {
        when (result) {
            is Continuous.OnNext -> {
                val min = TimeUnit.MILLISECONDS.toMinutes(result.value)
                val sec = TimeUnit.MILLISECONDS.toSeconds(result.value - TimeUnit.MINUTES.toMillis(min))

                tViewCountdown.text = String.format("%02d:%02d", min, sec)
            }
            is Continuous.OnComplete -> {
                btnResend.isEnabled = true
            }
        }
    }

    private fun resetPasswordObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> longToast(R.string.toastResend)
            is Completable.OnError -> longToast(R.string.toastTryAgainLater)
        }
    }

    private fun sendEmailVerificationObserver(result: Completable?) {
        when (result) {
            is Completable.OnComplete -> longToast(R.string.toastResend)
            is Completable.OnError -> longToast(R.string.toastTryAgainLater)
        }
    }
}
