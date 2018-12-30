package com.gdavidpb.tuindice.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.usecase.LoginUseCase
import com.gdavidpb.tuindice.domain.usecase.ResolveResourceUseCase
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import io.reactivex.observers.DisposableSingleObserver

class LoginActivityViewModel(
        private val loginUseCase: LoginUseCase,
        private val resolveResourceUseCase: ResolveResourceUseCase
) : ViewModel() {

    fun auth(request: AuthRequest, observer: DisposableSingleObserver<AuthResponse>) = loginUseCase.execute(observer, request)

    fun resolveResource(url: String, observer: DisposableSingleObserver<Uri>) = resolveResourceUseCase.execute(observer, url)

    override fun onCleared() {
        loginUseCase.dispose()
        resolveResourceUseCase.dispose()

        super.onCleared()
    }
}