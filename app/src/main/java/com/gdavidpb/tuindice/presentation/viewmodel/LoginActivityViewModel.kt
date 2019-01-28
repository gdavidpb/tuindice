package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.utils.LiveResult
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.usecase.LoginUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest

class LoginActivityViewModel(
        private val loginUseCase: LoginUseCase
) : ViewModel() {

    val auth = LiveResult<AuthResponse>()

    fun auth(request: AuthRequest) = loginUseCase.execute(liveData = auth, params = request)
}