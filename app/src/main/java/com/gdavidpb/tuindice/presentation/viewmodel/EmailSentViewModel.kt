package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.CountdownUseCase
import com.gdavidpb.tuindice.domain.usecase.ResendResetEmailUseCase
import com.gdavidpb.tuindice.domain.usecase.ResendVerifyEmailUseCase
import com.gdavidpb.tuindice.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.domain.usecase.request.CountdownRequest
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveFlow
import com.gdavidpb.tuindice.utils.extensions.execute

class EmailSentViewModel(
        private val resendResetEmailUseCase: ResendResetEmailUseCase,
        private val resendVerifyEmailUseCase: ResendVerifyEmailUseCase,
        private val signOutUseCase: SignOutUseCase,
        private val countdownUseCase: CountdownUseCase
) : ViewModel() {

    val countdown = LiveFlow<Long>()
    val signOut = LiveCompletable()
    val resetPassword = LiveCompletable()
    val sendEmailVerification = LiveCompletable()

    fun startCountdown(time: Long, reset: Boolean = false) =
            execute(useCase = countdownUseCase, params = CountdownRequest(time, reset), liveData = countdown)

    fun sendResetPasswordEmail() =
            execute(useCase = resendResetEmailUseCase, params = Unit, liveData = resetPassword)

    fun sendVerificationEmail() =
            execute(useCase = resendVerifyEmailUseCase, params = Unit, liveData = sendEmailVerification)

    fun signOut() =
            execute(useCase = signOutUseCase, params = Unit, liveData = signOut)
}