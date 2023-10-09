package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.ValidateAddEvaluationStep1UseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toValidateAddEvaluationStep1Params
import com.gdavidpb.tuindice.evaluations.presentation.reducer.AddEvaluationStep1Reducer
import com.gdavidpb.tuindice.evaluations.presentation.reducer.AvailableSubjectsReducer

class AddEvaluationViewModel(
	private val getAvailableSubjectsUseCase: GetAvailableSubjectsUseCase,
	private val availableSubjectsReducer: AvailableSubjectsReducer,
	private val validateAddEvaluationStep1UseCase: ValidateAddEvaluationStep1UseCase,
	private val addEvaluationStep1Reducer: AddEvaluationStep1Reducer
) : BaseViewModel<AddEvaluation.State, AddEvaluation.Action, AddEvaluation.Event>(initialViewState = AddEvaluation.State.Loading) {

	fun setSubject(subject: Subject) {
		val currentState = getCurrentState()

		if (currentState !is AddEvaluation.State.Step1) return

		setState(
			currentState.copy(
				subject = subject
			)
		)
	}

	fun setType(type: EvaluationType) {
		val currentState = getCurrentState()

		if (currentState !is AddEvaluation.State.Step1) return

		setState(
			currentState.copy(
				type = type
			)
		)
	}

	fun setDate(date: Long) {
		val currentState = getCurrentState()

		if (currentState !is AddEvaluation.State.Step2) return

		setState(
			currentState.copy(
				date = date
			)
		)
	}

	fun loadAvailableSubjectsAction() =
		emitAction(AddEvaluation.Action.LoadAvailableSubjects)

	fun goNextStepAction() {
		when (val currentState = getCurrentState()) {
			is AddEvaluation.State.Step1 ->
				emitAction(
					AddEvaluation.Action.ClickStep1Done(
						subject = currentState.subject,
						type = currentState.type
					)
				)

			is AddEvaluation.State.Step2 ->
				emitAction(
					AddEvaluation.Action.ClickStep2Done(
						subject = currentState.subject,
						type = currentState.type,
						date = null, // TODO
						grade = null,
						maxGrade = null
					)
				)

			else -> {}
		}
	}

	override suspend fun reducer(action: AddEvaluation.Action) {
		when (action) {
			is AddEvaluation.Action.LoadAvailableSubjects ->
				getAvailableSubjectsUseCase
					.execute(
						params = Unit
					)
					.collect(viewModel = this, reducer = availableSubjectsReducer)

			is AddEvaluation.Action.ClickStep1Done ->
				validateAddEvaluationStep1UseCase
					.execute(
						params = action.toValidateAddEvaluationStep1Params()
					)
					.collect(viewModel = this, reducer = addEvaluationStep1Reducer)

			is AddEvaluation.Action.ClickStep2Done -> {
				// TODO
			}
		}
	}
}