package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.utils.LiveCompletable
import com.gdavidpb.tuindice.utils.execute

class AboutViewModel(
        private val signOutUseCase: SignOutUseCase
) : ViewModel() {
    val signOut = LiveCompletable()

    fun signOut() =
            execute(useCase = signOutUseCase, params = Unit, liveData = signOut)

}