package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.domain.usecase.SyncAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute

class SignInViewModel(
        private val signInUseCase: SignInUseCase,
        private val syncAccountUseCase: SyncAccountUseCase
) : ViewModel() {

    val signIn = LiveCompletable<SignInError>()
    val sync = LiveResult<Boolean, SyncError>()

    fun signIn(usbId: String, password: String) {
        val credentials = Credentials(
                usbId = usbId,
                password = password
        )

        execute(useCase = signInUseCase, params = credentials, liveData = signIn)
    }

    fun syncAccount() =
            execute(useCase = syncAccountUseCase, params = Unit, liveData = sync)
}