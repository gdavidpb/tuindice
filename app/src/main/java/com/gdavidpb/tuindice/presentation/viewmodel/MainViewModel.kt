package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.annotation.IdRes
import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.StartUpAction
import com.gdavidpb.tuindice.domain.usecase.*
import com.gdavidpb.tuindice.domain.usecase.request.SubjectUpdateRequest
import com.gdavidpb.tuindice.domain.usecase.response.SyncResponse
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute
import java.io.File

class MainViewModel(
        private val signOutUseCase: SignOutUseCase,
        private val startUpUseCase: StartUpUseCase,
        private val syncAccountUseCase: SyncAccountUseCase,
        private val setLastScreenUseCase: SetLastScreenUseCase,
        private val getQuartersUseCase: GetQuartersUseCase,
        private val updateSubjectUseCase: UpdateSubjectUseCase,
        private val openEnrollmentProofUseCase: OpenEnrollmentProofUseCase
) : ViewModel() {
    val signOut = LiveCompletable()
    val fetchStartUpAction = LiveResult<StartUpAction>()
    val sync = LiveResult<SyncResponse>()
    val lastScreen = LiveCompletable()
    val quarters = LiveResult<List<Quarter>>()
    val subjectUpdate = LiveCompletable()
    val enrollment = LiveEvent<File>()

    fun signOut() =
            execute(useCase = signOutUseCase, params = Unit, liveData = signOut)

    fun fetchStartUpAction(dataString: String) =
            execute(useCase = startUpUseCase, params = dataString, liveData = fetchStartUpAction)

    fun trySyncAccount() =
            execute(useCase = syncAccountUseCase, params = Unit, liveData = sync)

    fun setLastScreen(@IdRes navId: Int) =
            execute(useCase = setLastScreenUseCase, params = navId, liveData = lastScreen)

    fun getQuarters() =
            execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)

    fun updateSubject(sid: String, grade: Int) =
            execute(useCase = updateSubjectUseCase, params = SubjectUpdateRequest(sid, grade), liveData = subjectUpdate)

    fun openEnrollmentProof() =
            execute(useCase = openEnrollmentProofUseCase, params = Unit, liveData = enrollment)
}
