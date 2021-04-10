package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.SignInResponse
import com.gdavidpb.tuindice.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.domain.usecase.SyncAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.domain.usecase.errors.SyncError
import com.gdavidpb.tuindice.domain.usecase.request.SignInRequest
import com.gdavidpb.tuindice.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute

class LoginViewModel(
        private val signInUseCase: SignInUseCase,
        private val syncAccountUseCase: SyncAccountUseCase
) : ViewModel() {

    val signIn = LiveEvent<SignInResponse, SignInError>()
    val sync = LiveResult<Boolean, SyncError>()

    fun signIn(usbId: String, password: String) {
        val request = SignInRequest(
                usbId = usbId,
                password = password,
                serviceUrl = BuildConfig.ENDPOINT_DST_RECORD_AUTH
        )

        execute(useCase = signInUseCase, params = request, liveData = signIn)
    }

    fun syncAccount() =
            execute(useCase = syncAccountUseCase, params = Unit, liveData = sync)
}