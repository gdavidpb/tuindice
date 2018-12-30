package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.GetLocalAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.LogoutUseCase
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableMaybeObserver

class MainActivityViewModel(
        private val getLocalAccountUseCase: GetLocalAccountUseCase,
        private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    fun getActiveAccount(observer: DisposableMaybeObserver<Account>): Maybe<Account> = getLocalAccountUseCase.execute(observer, null)

    fun logout(observer: DisposableCompletableObserver): Completable = logoutUseCase.execute(observer, null)

    override fun onCleared() {
        getLocalAccountUseCase.dispose()
        logoutUseCase.dispose()

        super.onCleared()
    }
}