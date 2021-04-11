package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Credentials
import com.gdavidpb.tuindice.domain.usecase.SignInUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.SignInError
import com.gdavidpb.tuindice.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.utils.extensions.execute

class SignInViewModel(
        private val signInUseCase: SignInUseCase
) : ViewModel() {
    val signIn = LiveEvent<Unit, SignInError>()

    fun signIn(usbId: String, password: String) {
        val credentials = Credentials(usbId = usbId, password = password)

        execute(useCase = signInUseCase, params = credentials, liveData = signIn)
    }
}