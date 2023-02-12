package com.gdavidpb.tuindice.record.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.gdavidpb.tuindice.base.utils.extension.stateInAction
import com.gdavidpb.tuindice.base.utils.extension.stateInFlow
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
	val removeQuarterParams = MutableSharedFlow<String>()
	val updateSubjectParams = MutableSharedFlow<UpdateSubjectParams>()
	val withdrawSubjectParams = MutableSharedFlow<String>()

	val getQuarters =
		stateInFlow(useCase = getQuartersUseCase, params = Unit)

	val removeQuarter =
		stateInAction(useCase = removeQuarterUseCase, paramsFlow = removeQuarterParams)

	val updateSubject =
		stateInAction(useCase = updateSubjectUseCase, paramsFlow = updateSubjectParams)

	val withdrawSubject =
		stateInAction(useCase = withdrawSubjectUseCase, paramsFlow = withdrawSubjectParams)
}