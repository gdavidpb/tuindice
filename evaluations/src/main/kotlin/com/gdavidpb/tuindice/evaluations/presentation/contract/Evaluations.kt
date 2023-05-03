package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationViewState

object Evaluations {
	sealed class State : ViewState {
		object Loading : State()
		class Loaded(val value: EvaluationViewState) : State()
		object Failed : State()
	}

	sealed class Action : ViewAction {
		class LoadEvaluation(val params: GetEvaluationParams) : Action()
		class AddEvaluation(val params: AddEvaluationParams) : Action()
		class UpdateEvaluation(val params: UpdateEvaluationParams) : Action()
	}

	sealed class Event : ViewEvent {
		object NavigateUp : Event()
		object ShowEmptyNameError : Event()
		object ShowInvalidGradeStepError : Event()
		object ShowOutOfRangeGradeError : Event()
		object ShowDateMissedError : Event()
		object ShowTypeMissedError : Event()
		object ShowDefaultErrorSnackBar : Event()
	}
}