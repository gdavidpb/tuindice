package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.domain.usecase.OpenEnrollmentProofUseCase
import com.gdavidpb.tuindice.domain.usecase.SyncAccountUseCase
import com.gdavidpb.tuindice.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.utils.LiveCompletable
import com.gdavidpb.tuindice.utils.LiveContinuous
import com.gdavidpb.tuindice.utils.LiveResult
import com.gdavidpb.tuindice.utils.execute
import java.io.File

class RecordViewModel(
        private val syncAccountUseCase: SyncAccountUseCase,
        private val getQuartersUseCase: GetQuartersUseCase,
        private val updateSubjectUseCase: UpdateSubjectUseCase,
        private val openEnrollmentProofUseCase: OpenEnrollmentProofUseCase
) : ViewModel() {
    val account = LiveContinuous<Account>()
    val quarters = LiveResult<List<Quarter>>()
    val update = LiveCompletable()
    val enrollment = LiveResult<File>()

    fun loadAccount(trySync: Boolean) =
            execute(useCase = syncAccountUseCase, params = trySync, liveData = account)

    fun getQuarters() =
            execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)


    fun updateSubject(subject: Subject) =
            execute(useCase = updateSubjectUseCase, params = subject, liveData = update)

    fun openEnrollmentProof() =
            execute(useCase = openEnrollmentProofUseCase, params = Unit, liveData = enrollment)

}