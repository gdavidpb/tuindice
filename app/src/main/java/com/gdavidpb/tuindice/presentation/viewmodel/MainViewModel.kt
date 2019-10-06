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
import com.gdavidpb.tuindice.utils.execute

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

    fun logout() =
            execute(useCase = logoutUseCase, params = Unit, liveData = logout)

    fun fetchStartUpAction(dataString: String) =
            execute(useCase = startUpUseCase, params = dataString, liveData = fetchStartUpAction)

    fun loadAccount(trySync: Boolean) =
            execute(useCase = syncAccountUseCase, params = trySync, liveData = account)

    fun setLastScreen(@IdRes navId: Int) =
            execute(useCase = setLastScreenUseCase, params = navId, liveData = lastScreen)

    fun getQuarters() =
            execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)

    fun updateSubject(subject: Subject) =
            execute(useCase = updateSubjectUseCase, params = subject, liveData = update)
}