package com.gdavidpb.tuindice.record.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.domain.usecase.baseV2.UseCaseState
import com.gdavidpb.tuindice.base.utils.extension.LiveCompletable
import com.gdavidpb.tuindice.base.utils.extension.LiveResult
import com.gdavidpb.tuindice.base.utils.extension.execute
import com.gdavidpb.tuindice.record.domain.error.SubjectError
import com.gdavidpb.tuindice.record.domain.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.record.domain.usecase.WithdrawSubjectUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class RecordViewModel(
	getQuartersUseCase: GetQuartersUseCase,
	private val removeQuarterUseCase: RemoveQuarterUseCase,
	private val updateSubjectUseCase: UpdateSubjectUseCase,
	private val withdrawSubjectUseCase: WithdrawSubjectUseCase
) : ViewModel() {
	val removeQuarter = LiveCompletable<Nothing>()
	val updateSubject = LiveResult<Subject, SubjectError>()
	val withdrawSubject = LiveCompletable<Nothing>()

	val getQuarters = getQuartersUseCase
		.execute(Unit)
		.stateIn(
			scope = viewModelScope,
			started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
			initialValue = UseCaseState.Loading()
		)

	fun removeQuarter(quarterId: String) =
		execute(useCase = removeQuarterUseCase, params = quarterId, liveData = removeQuarter)

	fun updateSubject(params: UpdateSubjectParams) =
		execute(useCase = updateSubjectUseCase, params = params, liveData = updateSubject)

	fun withdrawSubject(subjectId: String) =
		execute(useCase = withdrawSubjectUseCase, params = subjectId, liveData = withdrawSubject)
}