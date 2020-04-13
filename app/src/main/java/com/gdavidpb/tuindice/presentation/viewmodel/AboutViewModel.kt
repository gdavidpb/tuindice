package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.execute

class AboutViewModel(
        private val signOutUseCase: SignOutUseCase
) : ViewModel() {
    val signOut = LiveCompletable()

    fun signOut() =
            execute(useCase = signOutUseCase, params = Unit, liveData = signOut)
}