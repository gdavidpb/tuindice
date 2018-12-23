package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.GetAccountUseCase
import io.reactivex.Maybe
import io.reactivex.observers.DisposableMaybeObserver

class MainActivityViewModel(
        private val getAccountUseCase: GetAccountUseCase
) : ViewModel() {

    fun getActiveAccount(observer: DisposableMaybeObserver<Account>): Maybe<Account> = getAccountUseCase.execute(observer, null)

    override fun onCleared() {
        super.onCleared()

        getAccountUseCase.dispose(true)
    }
}