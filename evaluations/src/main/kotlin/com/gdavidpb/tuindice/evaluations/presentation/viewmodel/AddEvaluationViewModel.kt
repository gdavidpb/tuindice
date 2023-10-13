package com.gdavidpb.tuindice.evaluations.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.evaluations.domain.usecase.GetAvailableSubjectsUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.ValidateAddEvaluationStep1UseCase
import com.gdavidpb.tuindice.evaluations.presentation.contract.AddEvaluation
import com.gdavidpb.tuindice.evaluations.presentation.mapper.toValidateAddEvaluationStep1Params
import com.gdavidpb.tuindice.evaluations.presentation.reducer.LoadAddEvaluationStep1Reducer
import com.gdavidpb.tuindice.evaluations.presentation.reducer.DoneAddEvaluationStep1Reducer

class AddEvaluationViewModel(
	private val getAvailableSubjectsUseCase: GetAvailableSubjectsUseCase,
	private val loadAddEvaluationStep1Reducer: LoadAddEvaluationStep1Reducer,
	private val validateAddEvaluationStep1UseCase: ValidateAddEvaluationStep1UseCase,
	private val doneAddEvaluationStep1Reducer: DoneAddEvaluationStep1Reducer
) : BaseViewModel<AddEvaluation.State, AddEvaluation.Action, AddEvaluation.Event>(initialViewState = AddEvaluation.State.Step1()) {

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

	fun loadStep1Action() =
		emitAction(AddEvaluation.Action.LoadStep1)

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
			is AddEvaluation.Action.LoadStep1 ->
				getAvailableSubjectsUseCase
					.execute(
						params = Unit
					)
					.collect(viewModel = this, reducer = loadAddEvaluationStep1Reducer)

			is AddEvaluation.Action.ClickStep1Done ->
				validateAddEvaluationStep1UseCase
					.execute(
						params = action.toValidateAddEvaluationStep1Params()
					)
					.collect(viewModel = this, reducer = doneAddEvaluationStep1Reducer)

			is AddEvaluation.Action.LoadStep2 -> {
				// TODO
			}

			is AddEvaluation.Action.ClickStep2Done -> {
				// TODO
			}
		}
	}
}