package com.gdavidpb.tuindice.record.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extensions.LiveEvent
import com.gdavidpb.tuindice.base.utils.extensions.LiveResult
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.record.domain.error.GetEnrollmentError
import com.gdavidpb.tuindice.record.domain.usecase.GetEnrollmentProofUseCase
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase

class RecordViewModel(
	private val getQuartersUseCase: GetQuartersUseCase,
	private val getEnrollmentProofUseCase: GetEnrollmentProofUseCase,
	private val removeQuarterUseCase: RemoveQuarterUseCase
) : ViewModel() {
	val quarters = LiveResult<List<Quarter>, Nothing>()
	val enrollment = LiveEvent<String, GetEnrollmentError>()
	val quarterRemove = LiveCompletable<Nothing>()

	fun getQuarters() =
		execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)

	fun openEnrollmentProof(quarter: Quarter) =
		execute(useCase = getEnrollmentProofUseCase, params = quarter, liveData = enrollment)

	fun removeQuarter(id: String) =
		execute(useCase = removeQuarterUseCase, params = id, liveData = quarterRemove)
}