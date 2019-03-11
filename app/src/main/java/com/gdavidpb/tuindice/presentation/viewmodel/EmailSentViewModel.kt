package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.CountdownUseCase
import com.gdavidpb.tuindice.domain.usecase.ResendResetEmailUseCase
import com.gdavidpb.tuindice.domain.usecase.ResendVerifyEmailUseCase
import com.gdavidpb.tuindice.utils.LiveCompletable
import com.gdavidpb.tuindice.utils.LiveContinuous

class EmailSentViewModel(
        private val resendResetEmailUseCase: ResendResetEmailUseCase,
        private val resendVerifyEmailUseCase: ResendVerifyEmailUseCase,
        private val countdownUseCase: CountdownUseCase
) : ViewModel() {

    val countdown = LiveContinuous<Long>()
    val resetPassword = LiveCompletable()
    val sendEmailVerification = LiveCompletable()

    fun startCountdown(forceRestart: Boolean = false) {
        countdownUseCase.execute(liveData = countdown, params = forceRestart)
    }

    fun resetPassword() {
        resendResetEmailUseCase.execute(liveData = resetPassword, params = Unit)
    }

    fun sendEmailVerification() {
        resendVerifyEmailUseCase.execute(liveData = sendEmailVerification, params = Unit)
    }
}