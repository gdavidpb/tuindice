package com.gdavidpb.tuindice.presentation.viewmodel

import android.content.Intent
import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.LogoutUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastScreenUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.SyncAccountUseCase
import com.gdavidpb.tuindice.utils.LiveCompletable
import com.gdavidpb.tuindice.utils.LiveContinuous
import com.gdavidpb.tuindice.utils.LiveResult

class MainViewModel(
        private val logoutUseCase: LogoutUseCase,
        private val startUpUseCase: StartUpUseCase,
        private val syncAccountUseCase: SyncAccountUseCase,
        private val setLastScreenUseCase: SetLastScreenUseCase
) : ViewModel() {
    val logout = LiveCompletable()
    val fetchStartUpAction = LiveResult<StartUpAction>()
    val account = LiveContinuous<Account>()
    val lastScreen = LiveCompletable()

    fun logout() {
        logoutUseCase.execute(liveData = logout, params = Unit)
    }

    fun fetchStartUpAction(intent: Intent) {
        startUpUseCase.execute(liveData = fetchStartUpAction, params = intent)
    }

    fun loadAccount(trySync: Boolean) {
        syncAccountUseCase.execute(liveData = account, params = trySync)
    }

    fun setLastScreen(@IdRes fragment: Int) {
        setLastScreenUseCase.execute(liveData = lastScreen, params = fragment)
    }
}