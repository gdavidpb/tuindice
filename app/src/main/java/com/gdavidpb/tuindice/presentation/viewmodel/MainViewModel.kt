package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute
import java.io.File

class MainViewModel(
        private val signOutUseCase: SignOutUseCase,
        private val startUpUseCase: StartUpUseCase,
        private val syncAccountUseCase: SyncAccountUseCase,
        private val setLastScreenUseCase: SetLastScreenUseCase,
        private val getAccountUseCase: GetAccountUseCase,
        private val getQuartersUseCase: GetQuartersUseCase,
        private val updateSubjectUseCase: UpdateSubjectUseCase,
        private val openEnrollmentProofUseCase: OpenEnrollmentProofUseCase
) : ViewModel() {
    val signOut = LiveCompletable()
    val fetchStartUpAction = LiveResult<StartUpAction>()
    val sync = LiveResult<Boolean>()
    val lastScreen = LiveCompletable()
    val account = LiveResult<Account>()
    val quarters = LiveResult<List<Quarter>>()
    val update = LiveCompletable()
    val enrollment = LiveResult<File>()

    fun signOut() =
            execute(useCase = signOutUseCase, params = Unit, liveData = signOut)

    fun fetchStartUpAction(dataString: String) =
            execute(useCase = startUpUseCase, params = dataString, liveData = fetchStartUpAction)

    fun trySyncAccount() =
            execute(useCase = syncAccountUseCase, params = Unit, liveData = sync)

    fun setLastScreen(@IdRes navId: Int) =
            execute(useCase = setLastScreenUseCase, params = navId, liveData = lastScreen)

    fun getAccount() =
            execute(useCase = getAccountUseCase, params = Unit, liveData = account)

    fun getQuarters() =
            execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)

    fun updateSubject(subject: Subject) =
            execute(useCase = updateSubjectUseCase, params = subject, liveData = update)

    fun openEnrollmentProof() =
            execute(useCase = openEnrollmentProofUseCase, params = Unit, liveData = enrollment)
}
