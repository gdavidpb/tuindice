package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.reducer.AvailableSubjectsReducer

class AddEvaluationViewModel(
	private val getAvailableSubjectsUseCase: GetAvailableSubjectsUseCase,
	private val availableSubjectsReducer: AvailableSubjectsReducer
) : BaseViewModel<AddEvaluation.State, AddEvaluation.Action, AddEvaluation.Event>(initialViewState = AddEvaluation.State.Loading) {

	fun loadAvailableSubjectsAction() =
		emitAction(AddEvaluation.Action.LoadAvailableSubjects)

	override suspend fun reducer(action: AddEvaluation.Action) {
		when (action) {
			is AddEvaluation.Action.LoadAvailableSubjects ->
				getAvailableSubjectsUseCase
					.execute(
						params = Unit
					)
					.collect(viewModel = this, reducer = availableSubjectsReducer)

			is AddEvaluation.Action.ClickStep1Done ->
				TODO()

			is AddEvaluation.Action.ClickStep2Done ->
				TODO()
		}
	}
}