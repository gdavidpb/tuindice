package com.gdavidpb.tuindice.presentation.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.GetAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.LogoutUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.request.SignInWithLinkUseCase
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.observers.DisposableCompletableObserver
import io.reactivex.observers.DisposableMaybeObserver
import io.reactivex.observers.DisposableSingleObserver

class MainActivityViewModel(
        private val getAccountUseCase: GetAccountUseCase,
        private val logoutUseCase: LogoutUseCase,
        private val signInWithLinkUseCase: SignInWithLinkUseCase,
        private val startUpUseCase: StartUpUseCase
) : ViewModel() {

    fun getActiveAccount(observer: DisposableMaybeObserver<Account>, tryRefresh: Boolean): Maybe<Account> {
        return getAccountUseCase.execute(observer, params = tryRefresh)
    }

    fun logout(observer: DisposableCompletableObserver): Completable {
        return logoutUseCase.execute(observer, null)
    }

    fun signInWithLink(observer: DisposableCompletableObserver, link: String): Completable {
        return signInWithLinkUseCase.execute(observer, params = link)
    }

    fun fetchStartUpAction(observer: DisposableSingleObserver<StartUpAction>, intent: Intent): Single<StartUpAction> {
        return startUpUseCase.execute(observer, params = intent)
    }

    override fun onCleared() {
        getAccountUseCase.dispose()
        logoutUseCase.dispose()
        signInWithLinkUseCase.dispose()
        startUpUseCase.dispose()

        super.onCleared()
    }
}