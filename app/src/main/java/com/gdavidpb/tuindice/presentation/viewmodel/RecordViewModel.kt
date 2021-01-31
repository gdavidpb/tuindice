package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.domain.usecase.OpenEnrollmentProofUseCase
import com.gdavidpb.tuindice.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.domain.usecase.request.SubjectUpdateRequest
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute
import java.io.File

class RecordViewModel(
        private val getQuartersUseCase: GetQuartersUseCase,
        private val updateSubjectUseCase: UpdateSubjectUseCase,
        private val openEnrollmentProofUseCase: OpenEnrollmentProofUseCase
) : ViewModel() {
    val quarters = LiveResult<List<Quarter>, Any>()
    val subjectUpdate = LiveCompletable<Any>()
    val enrollment = LiveEvent<File, Any>()

    fun getQuarters() =
            execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)

    fun updateSubject(sid: String, grade: Int) =
            execute(useCase = updateSubjectUseCase, params = SubjectUpdateRequest(sid, grade), liveData = subjectUpdate)

    fun openEnrollmentProof() =
            execute(useCase = openEnrollmentProofUseCase, params = Unit, liveData = enrollment)
}