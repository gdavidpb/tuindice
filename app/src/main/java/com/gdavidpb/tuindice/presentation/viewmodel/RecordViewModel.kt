package com.gdavidpb.tuindice.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.usecase.GetEnrollmentProofUseCase
import com.gdavidpb.tuindice.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.domain.usecase.UpdateQuarterUseCase
import com.gdavidpb.tuindice.domain.usecase.errors.GetEnrollmentError
import com.gdavidpb.tuindice.domain.usecase.request.UpdateQuarterRequest
import com.gdavidpb.tuindice.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.utils.extensions.LiveResult
import com.gdavidpb.tuindice.utils.extensions.execute
import java.io.File

class RecordViewModel(
        private val getQuartersUseCase: GetQuartersUseCase,
        private val updateQuarterUseCase: UpdateQuarterUseCase,
        private val getEnrollmentProofUseCase: GetEnrollmentProofUseCase,
        private val removeQuarterUseCase: RemoveQuarterUseCase
) : ViewModel() {
    val quarters = LiveResult<List<Quarter>, Nothing>()
    val quarterUpdate = LiveResult<Quarter, Nothing>()
    val enrollment = LiveEvent<File, GetEnrollmentError>()
    val quarterRemove = LiveCompletable<Nothing>()

    fun getQuarters() =
            execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)

    fun updateQuarter(request: UpdateQuarterRequest) =
            execute(useCase = updateQuarterUseCase, params = request, liveData = quarterUpdate)

    fun openEnrollmentProof(quarter: Quarter) =
            execute(useCase = getEnrollmentProofUseCase, params = quarter, liveData = enrollment)

    fun removeQuarter(id: String) =
            execute(useCase = removeQuarterUseCase, params = id, liveData = quarterRemove)
}