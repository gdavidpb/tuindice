package com.gdavidpb.tuindice.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.utils.LiveCompletable
import com.gdavidpb.tuindice.utils.LiveResult
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.LogoutUseCase
import com.gdavidpb.tuindice.domain.usecase.ResetUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.request.ResetRequest

class MainActivityViewModel(
        private val getAccountUseCase: GetAccountUseCase,
        private val logoutUseCase: LogoutUseCase,
        private val startUpUseCase: StartUpUseCase,
        private val resetUseCase: ResetUseCase
) : ViewModel() {

    val getActiveAccount = LiveResult<Account>()
    val logout = LiveCompletable()
    val fetchStartUpAction = LiveResult<StartUpAction>()
    val resetPassword = LiveCompletable()

    fun getActiveAccount(tryRefresh: Boolean) {
        getAccountUseCase.execute(liveData = getActiveAccount, params = tryRefresh)
    }

    fun resetPassword(request: ResetRequest) {
        resetUseCase.execute(liveData = resetPassword, params = request)
    }

    fun logout() {
        logoutUseCase.execute(liveData = logout, params = Unit)
    }

    fun fetchStartUpAction(intent: Intent) {
        startUpUseCase.execute(liveData = fetchStartUpAction, params = intent)
    }
}