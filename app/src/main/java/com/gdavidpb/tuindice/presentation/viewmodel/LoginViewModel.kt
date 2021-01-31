package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.domain.usecase.SyncAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.AuthError
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute

class LoginViewModel(
        private val signInUseCase: SignInUseCase,
        private val syncAccountUseCase: SyncAccountUseCase
) : ViewModel() {

    val signIn = LiveResult<AuthResponse, Any>()
    val sync = LiveResult<Boolean, Any>()

    fun signIn(usbId: String, password: String) {
        val request = AuthRequest(
                usbId = usbId,
                password = password,
                serviceUrl = BuildConfig.ENDPOINT_DST_RECORD_AUTH
        )

        execute(useCase = signInUseCase, params = request, liveData = signIn)
    }

    fun trySyncAccount() =
            execute(useCase = syncAccountUseCase, params = Unit, liveData = sync)
}