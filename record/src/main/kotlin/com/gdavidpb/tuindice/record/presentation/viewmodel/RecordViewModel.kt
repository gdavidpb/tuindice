package com.gdavidpb.tuindice.record.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.stateInEagerly
import com.gdavidpb.tuindice.base.utils.extension.stateInWhileSubscribed
import com.gdavidpb.tuindice.record.domain.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.GetQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.RemoveQuarterUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSubjectUseCase
import com.gdavidpb.tuindice.record.domain.usecase.WithdrawSubjectUseCase
import kotlinx.coroutines.flow.MutableSharedFlow

class RecordViewModel(
	getQuartersUseCase: GetQuartersUseCase,
	removeQuarterUseCase: RemoveQuarterUseCase,
	updateSubjectUseCase: UpdateSubjectUseCase,
	withdrawSubjectUseCase: WithdrawSubjectUseCase
) : ViewModel() {
	val removeQuarterId = MutableSharedFlow<String>()
	val updateSubjectParams = MutableSharedFlow<UpdateSubjectParams>()
	val withdrawSubjectId = MutableSharedFlow<String>()

	val getQuarters =
		stateInWhileSubscribed(useCase = getQuartersUseCase, params = Unit)

	val removeQuarter =
		stateInEagerly(useCase = removeQuarterUseCase, paramsFlow = removeQuarterId)

	val updateSubject =
		stateInEagerly(useCase = updateSubjectUseCase, paramsFlow = updateSubjectParams)

	val withdrawSubject =
		stateInEagerly(useCase = withdrawSubjectUseCase, paramsFlow = withdrawSubjectId)
}