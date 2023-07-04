package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation

class EvaluationViewModel(
) : BaseViewModel<Evaluation.State, Evaluation.Action, Evaluation.Event>(initialViewState = Evaluation.State.Loading) {
	override suspend fun reducer(action: Evaluation.Action) {
	}
}