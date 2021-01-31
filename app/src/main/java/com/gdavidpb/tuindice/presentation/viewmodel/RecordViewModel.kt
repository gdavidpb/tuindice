package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.domain.usecase.GetEnrollmentProofUseCase
import com.gdavidpb.tuindice.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.GetEnrollmentError
import com.gdavidpb.tuindice.domain.usecase.request.SubjectUpdateRequest
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute
import java.io.File

class RecordViewModel(
        private val getQuartersUseCase: GetQuartersUseCase,
        private val updateSubjectUseCase: UpdateSubjectUseCase,
        private val getEnrollmentProofUseCase: GetEnrollmentProofUseCase
) : ViewModel() {
    val quarters = LiveResult<List<Quarter>, Nothing>()
    val subjectUpdate = LiveCompletable<Nothing>()
    val enrollment = LiveEvent<File, GetEnrollmentError>()

    fun getQuarters() =
            execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)

    fun updateSubject(sid: String, grade: Int) =
            execute(useCase = updateSubjectUseCase, params = SubjectUpdateRequest(sid, grade), liveData = subjectUpdate)

    fun openEnrollmentProof() =
            execute(useCase = getEnrollmentProofUseCase, params = Unit, liveData = enrollment)
}