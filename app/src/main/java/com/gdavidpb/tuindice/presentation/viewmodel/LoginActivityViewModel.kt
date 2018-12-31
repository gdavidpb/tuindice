package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.usecase.LoginUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import io.reactivex.observers.DisposableSingleObserver

class LoginActivityViewModel(
        private val loginUseCase: LoginUseCase
) : ViewModel() {

    fun auth(request: AuthRequest, observer: DisposableSingleObserver<AuthResponse>) = loginUseCase.execute(observer, request)

    override fun onCleared() {
        loginUseCase.dispose()

        super.onCleared()
    }
}