package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.utils.LiveCompletable
import com.gdavidpb.tuindice.utils.LiveContinuous
import com.gdavidpb.tuindice.utils.LiveResult

class MainViewModel(
        private val logoutUseCase: LogoutUseCase,
        private val startUpUseCase: StartUpUseCase,
        private val syncAccountUseCase: SyncAccountUseCase,
        private val setLastScreenUseCase: SetLastScreenUseCase,
        private val getQuartersUseCase: GetQuartersUseCase,
        private val updateSubjectUseCase: UpdateSubjectUseCase
) : ViewModel() {
    val logout = LiveCompletable()
    val fetchStartUpAction = LiveResult<StartUpAction>()
    val account = LiveContinuous<Account>()
    val lastScreen = LiveCompletable()
    val quarters = LiveResult<List<Quarter>>()
    val update = LiveCompletable()

    fun logout() {
        logoutUseCase.execute(liveData = logout, params = Unit)
    }

    fun fetchStartUpAction(dataString: String) {
        startUpUseCase.execute(liveData = fetchStartUpAction, params = dataString)
    }

    fun loadAccount(trySync: Boolean) {
        syncAccountUseCase.execute(liveData = account, params = trySync)
    }

    fun setLastScreen(@IdRes fragment: Int) {
        setLastScreenUseCase.execute(liveData = lastScreen, params = fragment)
    }

    fun getQuarters() {
        getQuartersUseCase.execute(liveData = quarters, params = Unit)
    }

    fun updateSubject(subject: Subject) {
        updateSubjectUseCase.execute(liveData = update, params = subject)
    }
}