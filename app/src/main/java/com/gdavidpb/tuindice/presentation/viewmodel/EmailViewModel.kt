package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.CountdownUseCase
import com.gdavidpb.tuindice.domain.usecase.SendResetPasswordEmailUseCase
import com.gdavidpb.tuindice.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SendResetPasswordEmailError
import com.gdavidpb.tuindice.domain.usecase.request.CountdownRequest
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveFlow
import com.gdavidpb.tuindice.utils.extensions.execute

class EmailViewModel(
        private val sendResetPasswordEmailUseCase: SendResetPasswordEmailUseCase,
        private val signOutUseCase: SignOutUseCase,
        private val countdownUseCase: CountdownUseCase
) : ViewModel() {
    val countdown = LiveFlow<Long, Nothing>()
    val signOut = LiveCompletable<Nothing>()
    val resetPasswordEmail = LiveCompletable<SendResetPasswordEmailError>()

    fun startCountdown(time: Long, reset: Boolean = false) =
            execute(useCase = countdownUseCase, params = CountdownRequest(time, reset), liveData = countdown)

    fun sendResetPasswordEmail() =
            execute(useCase = sendResetPasswordEmailUseCase, params = Unit, liveData = resetPasswordEmail)

    fun signOut() =
            execute(useCase = signOutUseCase, params = Unit, liveData = signOut)
}