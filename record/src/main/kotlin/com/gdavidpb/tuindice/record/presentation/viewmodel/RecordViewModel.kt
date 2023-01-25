package com.gdavidpb.tuindice.record.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.extensions.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extensions.LiveResult
import com.gdavidpb.tuindice.base.utils.extensions.execute
import com.gdavidpb.tuindice.record.domain.error.GetQuartersError
import com.gdavidpb.tuindice.record.domain.error.SubjectError
import com.gdavidpb.tuindice.record.domain.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSubjectUseCase

class RecordViewModel(
	private val getQuartersUseCase: GetQuartersUseCase,
	private val removeQuarterUseCase: RemoveQuarterUseCase,
	private val updateSubjectUseCase: UpdateSubjectUseCase
) : ViewModel() {
	val quarters = LiveResult<List<Quarter>, GetQuartersError>()
	val quarterRemove = LiveCompletable<Nothing>()
	val updateSubject = LiveResult<Subject, SubjectError>()

	fun getQuarters() =
		execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)

	fun removeQuarter(quarterId: String) =
		execute(useCase = removeQuarterUseCase, params = quarterId, liveData = quarterRemove)

	fun updateSubject(params: UpdateSubjectParams) =
		execute(useCase = updateSubjectUseCase, params = params, liveData = updateSubject)
}