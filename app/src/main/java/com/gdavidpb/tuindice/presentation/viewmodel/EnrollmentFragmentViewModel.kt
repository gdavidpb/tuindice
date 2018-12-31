package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.usecase.GetAccountUseCase
import io.reactivex.observers.DisposableMaybeObserver

open class EnrollmentFragmentViewModel(
        private val getAccountUseCase: GetAccountUseCase
) : ViewModel() {

    fun loadAccount(observer: DisposableMaybeObserver<Account>, tryRefresh: Boolean) {
        getAccountUseCase.execute(observer, params = tryRefresh)
    }

    override fun onCleared() {
        getAccountUseCase.dispose()

        super.onCleared()
    }
}