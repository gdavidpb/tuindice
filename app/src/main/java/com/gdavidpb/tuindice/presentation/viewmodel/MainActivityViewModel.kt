package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.LogoutUseCase
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableMaybeObserver

class MainActivityViewModel(
        private val getAccountUseCase: GetAccountUseCase,
        private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    fun getActiveAccount(observer: DisposableMaybeObserver<Account>, tryRefresh: Boolean): Maybe<Account> {
        return getAccountUseCase.execute(observer, params = tryRefresh)
    }

    fun logout(observer: DisposableCompletableObserver): Completable {
        return logoutUseCase.execute(observer, null)
    }

    override fun onCleared() {
        getAccountUseCase.dispose()
        logoutUseCase.dispose()

        super.onCleared()
    }
}