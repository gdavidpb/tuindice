package com.gdavidpb.tuindice.ui.activities

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.domain.usecase.coroutines.Completable
import com.gdavidpb.tuindice.domain.usecase.coroutines.Continuous
import com.gdavidpb.tuindice.presentation.viewmodel.EmailSentViewModel
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.animate
import com.gdavidpb.tuindice.utils.extensions.observe
import com.gdavidpb.tuindice.utils.extensions.onClickOnce
import com.gdavidpb.tuindice.utils.extensions.toCountdown
import kotlinx.android.synthetic.main.activity_email_sent.*
import org.jetbrains.anko.longToast
import org.koin.androidx.viewmodel.ext.android.viewModel

class EmailSentActivity : AppCompatActivity() {

    private val viewModel by viewModel<EmailSentViewModel>()

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
        val email = intent.getStringExtra(EXTRA_AWAITING_EMAIL)

        when (intent.getIntExtra(EXTRA_AWAITING_STATE, 0)) {
            FLAG_RESET -> {
                iViewLogo.setImageResource(R.drawable.ic_reset)
                tViewEmailTitle.text = getString(R.string.label_reset)
                tViewEmailMessage.text = getString(R.string.message_reset, email)
            }
            FLAG_VERIFY -> {
                iViewLogo.setImageResource(R.drawable.ic_verify)
                tViewEmailTitle.text = getString(R.string.label_verify)
                tViewEmailMessage.text = getString(R.string.message_verify, email)
            }
        }

        btnResend.onClickOnce(::onResendClick)
    }

    private fun onResendClick() {
        btnResend.isEnabled = false

        viewModel.startCountdown(forceRestart = true)

        when (intent.getIntExtra(EXTRA_AWAITING_STATE, 0)) {
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
