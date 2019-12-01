package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.CountdownUseCase
import com.gdavidpb.tuindice.domain.usecase.ResendResetEmailUseCase
import com.gdavidpb.tuindice.domain.usecase.ResendVerifyEmailUseCase
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveContinuous
import com.gdavidpb.tuindice.utils.extensions.execute

class EmailSentViewModel(
        private val resendResetEmailUseCase: ResendResetEmailUseCase,
        private val resendVerifyEmailUseCase: ResendVerifyEmailUseCase,
        private val countdownUseCase: CountdownUseCase
) : ViewModel() {

    val countdown = LiveContinuous<Long>()
    val resetPassword = LiveCompletable()
    val sendEmailVerification = LiveCompletable()

    fun startCountdown(forceRestart: Boolean = false) =
            execute(useCase = countdownUseCase, params = forceRestart, liveData = countdown)

    fun resetPassword() =
            execute(useCase = resendResetEmailUseCase, params = Unit, liveData = resetPassword)

    fun sendEmailVerification() =
            execute(useCase = resendVerifyEmailUseCase, params = Unit, liveData = sendEmailVerification)
}