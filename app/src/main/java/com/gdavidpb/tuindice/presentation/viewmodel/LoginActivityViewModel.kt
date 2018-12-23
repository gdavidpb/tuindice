package com.gdavidpb.tuindice.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.ResolveResourceUseCase
import io.reactivex.observers.DisposableSingleObserver

class LoginActivityViewModel(
        private val resolveResourceUseCase: ResolveResourceUseCase
) : ViewModel() {

    fun resolveResource(url: String, observer: DisposableSingleObserver<Uri>) = resolveResourceUseCase.execute(observer, url)

    override fun onCleared() {
        super.onCleared()

        resolveResourceUseCase.dispose(true)
    }
}