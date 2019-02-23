package com.gdavidpb.tuindice.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.LogoutUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.utils.LiveCompletable
import com.gdavidpb.tuindice.utils.LiveResult

class MainActivityViewModel(
        private val logoutUseCase: LogoutUseCase,
        private val startUpUseCase: StartUpUseCase
) : ViewModel() {
    val logout = LiveCompletable()
    val fetchStartUpAction = LiveResult<StartUpAction>()

    fun logout() {
        logoutUseCase.execute(liveData = logout, params = Unit)
    }

    fun fetchStartUpAction(intent: Intent) {
        startUpUseCase.execute(liveData = fetchStartUpAction, params = intent)
    }
}