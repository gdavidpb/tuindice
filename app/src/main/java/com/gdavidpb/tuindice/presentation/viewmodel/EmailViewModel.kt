package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.CountdownUseCase
import com.gdavidpb.tuindice.domain.usecase.ResendResetPasswordEmailUseCase
import com.gdavidpb.tuindice.domain.usecase.ResendVerifyEmailUseCase
import com.gdavidpb.tuindice.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SendResetPasswordEmailError
import com.gdavidpb.tuindice.domain.usecase.errors.SendVerificationEmailError
import com.gdavidpb.tuindice.domain.usecase.request.CountdownRequest
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveFlow
import com.gdavidpb.tuindice.utils.extensions.execute

class EmailViewModel(
        private val resendResetPasswordEmailUseCase: ResendResetPasswordEmailUseCase,
        private val resendVerifyEmailUseCase: ResendVerifyEmailUseCase,
        private val signOutUseCase: SignOutUseCase,
        private val countdownUseCase: CountdownUseCase
) : ViewModel() {

    val countdown = LiveFlow<Long, Nothing>()
    val signOut = LiveCompletable<Nothing>()
    val resetPasswordEmail = LiveCompletable<SendResetPasswordEmailError>()
    val verificationEmail = LiveCompletable<SendVerificationEmailError>()

    fun startCountdown(time: Long, reset: Boolean = false) =
            execute(useCase = countdownUseCase, params = CountdownRequest(time, reset), liveData = countdown)

    fun sendResetPasswordEmail() =
            execute(useCase = resendResetPasswordEmailUseCase, params = Unit, liveData = resetPasswordEmail)

    fun sendVerificationEmail() =
            execute(useCase = resendVerifyEmailUseCase, params = Unit, liveData = verificationEmail)

    fun signOut() =
            execute(useCase = signOutUseCase, params = Unit, liveData = signOut)
}