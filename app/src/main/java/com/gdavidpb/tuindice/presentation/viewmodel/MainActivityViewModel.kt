package com.gdavidpb.tuindice.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.data.utils.LiveCompletable
import com.gdavidpb.tuindice.data.utils.LiveResult
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.LogoutUseCase
import com.gdavidpb.tuindice.domain.usecase.SignInWithLinkUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase

class MainActivityViewModel(
        private val getAccountUseCase: GetAccountUseCase,
        private val logoutUseCase: LogoutUseCase,
        private val signInWithLinkUseCase: SignInWithLinkUseCase,
        private val startUpUseCase: StartUpUseCase
) : ViewModel() {

    val getActiveAccount = LiveResult<Account>()
    val logout = LiveCompletable()
    val signInWithLink = LiveCompletable()
    val fetchStartUpAction = LiveResult<StartUpAction>()

    fun getActiveAccount(tryRefresh: Boolean) {
        getAccountUseCase.execute(liveData = getActiveAccount, params = tryRefresh)
    }

    fun logout() {
        logoutUseCase.execute(liveData = logout, params = Unit)
    }

    fun signInWithLink(link: String) {
        signInWithLinkUseCase.execute(liveData = signInWithLink, params = link)
    }

    fun fetchStartUpAction(intent: Intent) {
        startUpUseCase.execute(liveData = fetchStartUpAction, params = intent)
    }
}