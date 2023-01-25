package com.gdavidpb.tuindice.record.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.extension.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extension.LiveResult
import com.gdavidpb.tuindice.base.utils.extension.execute
import com.gdavidpb.tuindice.record.domain.error.GetQuartersError
import com.gdavidpb.tuindice.record.domain.error.SubjectError
import com.gdavidpb.tuindice.record.domain.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.WithdrawSubjectUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSubjectUseCase

class RecordViewModel(
	private val getQuartersUseCase: GetQuartersUseCase,
	private val removeQuarterUseCase: RemoveQuarterUseCase,
	private val updateSubjectUseCase: UpdateSubjectUseCase,
	private val withdrawSubjectUseCase: WithdrawSubjectUseCase
) : ViewModel() {
	val quarters = LiveResult<List<Quarter>, GetQuartersError>()
	val removeQuarter = LiveCompletable<Nothing>()
	val updateSubject = LiveResult<Subject, SubjectError>()
	val withdrawSubject = LiveCompletable<Nothing>()

	fun getQuarters() =
		execute(useCase = getQuartersUseCase, params = Unit, liveData = quarters)

	fun removeQuarter(quarterId: String) =
		execute(useCase = removeQuarterUseCase, params = quarterId, liveData = removeQuarter)

	fun updateSubject(params: UpdateSubjectParams) =
		execute(useCase = updateSubjectUseCase, params = params, liveData = updateSubject)

	fun withdrawSubject(subjectId: String) =
		execute(useCase = withdrawSubjectUseCase, params = subjectId, liveData = withdrawSubject)
}